package com.awsgbsa.sigma_BE.user.service;

import com.awsgbsa.sigma_BE.exception.CustomException;
import com.awsgbsa.sigma_BE.exception.ErrorCode;
import com.awsgbsa.sigma_BE.security.jwt.JwtUtil;
import com.awsgbsa.sigma_BE.user.dto.LoginResponse;
import com.awsgbsa.sigma_BE.user.dto.ReissueRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    public LoginResponse reissueToken(ReissueRequest reissueRequest){
        String refreshToken = reissueRequest.getRefreshToken();

        jwtUtil.validateRefreshToken(refreshToken); // 토큰 유효성 확인
        Long userId = jwtUtil.extractUserId(refreshToken, true); // 유저Id 추출

        // 저장된 refreshToken 검증
        String savedRefreshToken = redisTemplate.opsForValue().get("RT:" + userId);
        if( savedRefreshToken==null ){ // 만료되어 삭제된 refreshtoken, 로그아웃한 유저
            log.error("[REISSUE] userId={} - 저장된 refreshToken 없음 - 만료된경우, 로그아웃한 경우", userId);
            throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED); // 클라이언트에 다시 로그인 요청
        }
        if( !savedRefreshToken.equals(refreshToken) ){
            log.error("[REISSUE] userId={} - 저장된 refreshToken과 불일치", userId);
            throw new CustomException(ErrorCode.MISMATCHED_REFRESH_TOKEN); // 불일치
        }

        // 유저를 조회하여 accessToken 생성
        String newAccessToken = jwtUtil.createAccessToken(userId);

        // refreshToken 만료 임박시 재발급
        Date expiration = jwtUtil.extractExpiration(refreshToken, true);
        long daysLeft = Duration.between(Instant.now(), expiration.toInstant()).toDays();

        String newRefreshToken = refreshToken;
        if(daysLeft < 3){ // 임박 기준 : 3일 미만
            newRefreshToken = jwtUtil.createRefreshToken(userId);
            log.info("[REISSUE] userId={} - refreshToken 재발급 (만료 {}일 남음)", userId, daysLeft);
            try{
                redisTemplate.opsForValue().set("RT:" + userId, newRefreshToken,
                        Duration.ofSeconds(jwtUtil.getRefreshTokenValidityInSeconds()));
            } catch (Exception e){
                log.error("[REISSUE] userId={} - Redis 저장 실패", userId, e);
                throw new CustomException(ErrorCode.REDIS_SAVE_FAIL);
            }
        }

        return new LoginResponse(newAccessToken, newRefreshToken);
    }

    public void logout(Long userId) {
        redisTemplate.delete("RT:" + userId);
    }
}
