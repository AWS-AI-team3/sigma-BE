package com.awsgbsa.sigma_BE.user.service;

import com.awsgbsa.sigma_BE.exception.CustomException;
import com.awsgbsa.sigma_BE.exception.ErrorCode;
import com.awsgbsa.sigma_BE.security.jwt.JwtUtil;
import com.awsgbsa.sigma_BE.user.domain.LoginProvider;
import com.awsgbsa.sigma_BE.user.domain.SubscriptStatus;
import com.awsgbsa.sigma_BE.user.domain.User;
import com.awsgbsa.sigma_BE.user.dto.GoogleLoginRequest;
import com.awsgbsa.sigma_BE.user.dto.LoginResponse;
import com.awsgbsa.sigma_BE.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleOauthService {
    private final JwtUtil jwtUtil;
    private final WebClient webClient;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    @Value("${google.token-uri}")
    private String tokenUri;

    @Value("${google.user-info-uri}")
    private String userInfoUri;

    @Transactional
    public LoginResponse login(GoogleLoginRequest loginRequest) {
        // 1. 구글 토큰 요청
        String token = getAccessTokenFromGoogle(loginRequest.getCode());

        // 2. 구글 유저 정보 요청
        Map<String, Object> userInfo = getUserInfo(token);
        String name = (String) userInfo.get("name");
        String email = (String) userInfo.get("email");
        String googleId = (String) userInfo.get("sub");
        String profileUrl = (String) userInfo.get("picture");

        // 3. DB에 유저 확인 or 생성
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .userName(name)
                        .loginProvider(LoginProvider.GOOGLE)
                        .createdAt(LocalDateTime.now())
                        .subscriptStatus(SubscriptStatus.FREE)
                        .profileUrl(profileUrl)
                        .build()));

        // 4. JWT 발급
        String jwtAccessToken = jwtUtil.createAccessToken(user.getId());
        String jwtRefreshToken = jwtUtil.createRefreshToken(user.getId());

        // Redis에 refreshToken저장
        try {
            // Redis에 refreshToken 저장
            redisTemplate.opsForValue().set("RT:" + user.getId(), jwtRefreshToken, Duration.ofDays(14));
        } catch (Exception e) {
            throw new CustomException(ErrorCode.REDIS_SAVE_FAIL);
        }

        return new LoginResponse(jwtAccessToken, jwtRefreshToken);
    }

    // Google에서 AccessToken을 가져오기
    private String getAccessTokenFromGoogle(String code) {
        try {
            // URL 디코딩
            String decodedCode = URLDecoder.decode(code, "UTF-8");

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "authorization_code");
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("code", decodedCode);  // 디코딩된 code 사용
            formData.add("redirect_uri", redirectUri);

            return webClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .doOnNext(body -> System.out.println("Google Token Response: " + body))
                    .map(response -> (String) response.get("access_token"))
                    .block();
        } catch (UnsupportedEncodingException e) {
            // 예외 처리 로직
            throw new RuntimeException("Code decoding failed", e);
        }
    }

    // Google에서 사용자정보 가져오기
    private Map<String, Object> getUserInfo(String accessToken) {
        return webClient.get()
                .uri(userInfoUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    if (response == null || response.get("sub") == null) {
                        throw new RuntimeException("Google user info response is invalid or missing required fields.");
                    }
                    return response; // 정상적인 응답 반환
                })
                .block();
    }
}
