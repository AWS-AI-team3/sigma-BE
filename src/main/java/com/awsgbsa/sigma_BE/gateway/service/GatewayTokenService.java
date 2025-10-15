package com.awsgbsa.sigma_BE.gateway.service;

import com.awsgbsa.sigma_BE.exception.CustomException;
import com.awsgbsa.sigma_BE.exception.ErrorCode;
import com.awsgbsa.sigma_BE.gateway.dto.GatewayTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Service
public class GatewayTokenService {

    @Value("${gateway-token.key}")
    private String tokenSecret;

    @Value("${gateway-token.algorithm}")
    private String algorithm;

    public GatewayTokenResponse createGatewayToken(Long userId, long ttlSeconds){
        try {
            long exp = Instant.now().toEpochMilli() + ttlSeconds * 1000;
            String playload = userId + ":" + exp;
            String sig = sign(playload);

            String raw = userId + ":" + exp + ":" + sig;
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes());

            return GatewayTokenResponse.builder()
                    .gatewayToken(encoded)
                    .build();
        } catch (Exception e){
            log.error("[GatewayTokenService] 토큰 생성실패", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String sign(String payload){
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(tokenSecret.getBytes(), algorithm));
            byte[] sigBytes = mac.doFinal(payload.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(sigBytes);
        } catch (NoSuchAlgorithmException e){
            log.error("[GatewayTokenService] 유효하지 않은 토큰 암호화 알고리즘");
            throw new CustomException(ErrorCode.INVALID_SIGN_ALGORITHM);
        } catch (InvalidKeyException e) {
            log.error("[GatewayTokenService] 유효하지 않은 토큰 암호화 키");
            throw new CustomException(ErrorCode.INVALID_SIGN_KEY);
        }
    }

    public boolean validateGatewayToken(String gatewayToken) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(gatewayToken));
            String[] parts = decoded.split(":");

            // 기본구조 검증
            if (parts.length != 3) {
                log.warn("[GatewayTokenService] 잘못된 토큰 형식입니다.");
                throw new CustomException(ErrorCode.INVALID_GATEWAY_TOKEN);
            }

            String userId = parts[0];
            long exp = Long.parseLong(parts[1]);
            String sig = parts[2];
            String payload = userId + ":" + exp;

            // 만료시간 검증
            if (exp <= System.currentTimeMillis()) {
                log.warn("[GatewayTokenService] GatewayToken 만료 (exp={}, now={})", exp, System.currentTimeMillis());
                throw new CustomException(ErrorCode.TOKEN_EXPIRED);
            }

            // 서명검증
            String calcSig = sign(payload);
            boolean valid = calcSig.equals(sig);

            if (!valid) {
                log.warn("[GatewayTokenService] GatewayToken 서명 불일치: userId={}, exp={}", userId, exp);
                throw new CustomException(ErrorCode.INVALID_GATEWAY_TOKEN);
            }

            log.info("[GatewayTokenService] GatewayToken 검증 성공: userId={}, exp={}", userId, exp);
            return true;
        } catch (CustomException e) {
            // 이미 정의된 CustomException은 그대로 전달
            throw e;
        } catch (Exception e) {
            log.error("[GatewayTokenService] 토큰 유효성검증 과정이 실패했습니다.", e);
            return false;
        }
    }
}
