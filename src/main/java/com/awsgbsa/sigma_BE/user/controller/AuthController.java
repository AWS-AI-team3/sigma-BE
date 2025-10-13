package com.awsgbsa.sigma_BE.user.controller;

import com.awsgbsa.sigma_BE.common.ApiResponse;
import com.awsgbsa.sigma_BE.security.jwt.JwtUtil;
import com.awsgbsa.sigma_BE.user.dto.GoogleLoginRequest;
import com.awsgbsa.sigma_BE.user.dto.LoginResponse;
import com.awsgbsa.sigma_BE.user.dto.ReissueRequest;
import com.awsgbsa.sigma_BE.user.service.AuthService;
import com.awsgbsa.sigma_BE.user.service.GoogleOauthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자 인증", description = "사용자 인증 관련 API")
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final JwtUtil jwtUtil;
    private final GoogleOauthService googleOauthService;
    private final AuthService authService;

    @PostMapping("/v1/auth/google/login")
    @Operation(summary = "Google OAuth 로그인(Authorization Code Flow 기반)",
            description = "구글 로그인에 성공하면, 액세스 토큰과 리프레시 토큰을 발급받아 응답으로 전달(accessToken=유효 15분, refreshToken=유효 14일)")
    public ResponseEntity<ApiResponse<?>> loginViaGoogle(
            @Parameter(description = "구글 로그인에 필요한 code값", required = true)
            @RequestBody GoogleLoginRequest loginRequest
    ){
        LoginResponse loginResponse = googleOauthService.loginWithIdToken(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }

    @PostMapping("/v2/auth/google/login")
    @Operation(summary = "Google OAuth 로그인(iOS Client ID 기반)",
            description = "구글 로그인에 성공하면, 액세스 토큰과 리프레시 토큰을 발급받아 응답으로 전달(accessToken=유효 15분, refreshToken=유효 14일)")
    public ResponseEntity<ApiResponse<?>> loginViaGoogle2(
            @Parameter(description = "구글 로그인에 필요한 code값", required = true)
            @RequestBody GoogleLoginRequest loginRequest
    ){
        LoginResponse loginResponse = googleOauthService.loginWithIdToken2(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }

    @PostMapping("/v1/auth/reissue")
    @Operation(summary = "토큰재발급 요청", description = "전달받은 refreshToken의 유효성을 확인하고 유효한 새로운 accessToken을 발급하여 전달")
    public ResponseEntity<ApiResponse<?>> reissueToken(
            @Parameter(description = "재발급 요청을 위한 refreshToken값", required = true)
            @RequestBody ReissueRequest reissueRequest
    ){
        LoginResponse newLoginResponse = authService.reissueToken(reissueRequest);
        return ResponseEntity.ok(ApiResponse.success(newLoginResponse));
    }

    @PostMapping("/v1/auth/logout")
    @Operation(summary = "로그아웃 요청", description = "현재 로그인한 사용자의 리프레시 토큰을 삭제")
    public ResponseEntity<ApiResponse<?>> logout(HttpServletRequest request){
        String token = jwtUtil.resolveToken(request);
        Long userId = jwtUtil.extractUserId(token, false);
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.success("로그아웃 완료"));
    }
}