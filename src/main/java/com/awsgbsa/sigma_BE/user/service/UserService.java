package com.awsgbsa.sigma_BE.user.service;

import com.awsgbsa.sigma_BE.exception.CustomException;
import com.awsgbsa.sigma_BE.exception.ErrorCode;
import com.awsgbsa.sigma_BE.user.domain.User;
import com.awsgbsa.sigma_BE.user.dto.UserInfoResponse;
import com.awsgbsa.sigma_BE.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;

    public User findByUserId(Long userId) {
        return userRepository.findOne(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public UserInfoResponse getUserInfo(Long userId) {
        User user = findByUserId(userId);
        return UserInfoResponse.builder()
                .userName(user.getUserName())
                .profileUrl(user.getProfileUrl())
                .subscriptStatus(user.getSubscriptStatus().name())
                .build();
    }
}
