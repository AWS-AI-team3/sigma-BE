package com.awsgbsa.sigma_BE.face.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FaceSessionService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String PREFIX = "faceSession:";

    // 얼굴인증 세션 생성
    public String createSession(Long userId){
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(PREFIX + token, String.valueOf(userId));
        return token;
    }

    // 세션 유효성 검증
    public boolean validateSession(String token, Long userId) {
        String value = redisTemplate.opsForValue().get(PREFIX + token);
        return value != null && value.equals(String.valueOf(userId));
    }

    // 세션 종료
    public void endSession(String token) {
        redisTemplate.delete(PREFIX + token);
    }
}
