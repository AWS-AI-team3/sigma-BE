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


}
