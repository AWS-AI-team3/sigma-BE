package com.awsgbsa.sigma_BE.user.controller;

import com.awsgbsa.sigma_BE.common.ApiResponse;
import com.awsgbsa.sigma_BE.security.jwt.JwtUtil;
import com.awsgbsa.sigma_BE.user.dto.UserInfoResponse;
import com.awsgbsa.sigma_BE.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 조회", description = "사용자 정보조회 API")
@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/info")
    @Operation(summary = "유저 기본정보 조회", description = "유저의 이름, 이메일, 프로필사진URL, 무료 혹은 유료구독여부를 반환합니다.")
    public ResponseEntity<ApiResponse<?>> getUserInfo(
            HttpServletRequest request
    ){
        String token = jwtUtil.resolveToken(request);
        Long userId = jwtUtil.extractUserId(token, false);
        UserInfoResponse userInfo = userService.getUserInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }
}
