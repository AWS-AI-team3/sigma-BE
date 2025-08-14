package com.awsgbsa.sigma_BE.face.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FaceSessionService {
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${face-session.prefix}")
    private String prefix;

    @Value("${face-session.session-ttl-minutes}")
    private long ttlMinutes;

    private String getKey(Long userId) {
        return prefix + ":" + userId;
    }

    // 얼굴인증 세션 자동등록
    public void createSession(Long userId){
        redisTemplate.opsForValue().set(getKey(userId), "verified", ttlMinutes, TimeUnit.MINUTES);
    }

    // 세션 유효성 검증
    public boolean validateSession(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(getKey(userId)));
    }

}
