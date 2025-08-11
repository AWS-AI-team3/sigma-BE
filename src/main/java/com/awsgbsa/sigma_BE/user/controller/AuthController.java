package com.awsgbsa.sigma_BE.user.controller;

import com.awsgbsa.sigma_BE.common.ApiResponse;
import com.awsgbsa.sigma_BE.security.jwt.JwtUtil;
import com.awsgbsa.sigma_BE.user.dto.GoogleLoginRequest;
import com.awsgbsa.sigma_BE.user.dto.LoginResponse;
import com.awsgbsa.sigma_BE.user.dto.ReissueRequest;
import com.awsgbsa.sigma_BE.user.service.AuthService;
import com.awsgbsa.sigma_BE.user.service.GoogleOauthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/v1/auth/")
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final JwtUtil jwtUtil;
    private final GoogleOauthService googleOauthService;
    private final AuthService authService;

    @PostMapping("/google/login")
    public ResponseEntity<ApiResponse<?>> loginViaGoogle(
            @RequestBody GoogleLoginRequest loginRequest
    ){
        LoginResponse loginResponse = googleOauthService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<?>> reissueToken(
            @RequestBody ReissueRequest reissueRequest
    ){
        LoginResponse newLoginResponse = authService.reissueToken(reissueRequest);
        return ResponseEntity.ok(ApiResponse.success(newLoginResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(HttpServletRequest request){
        String token = jwtUtil.resolveToken(request);
        Long userId = jwtUtil.extractUserId(token, false);
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.success("로그아웃 완료"));
    }
}