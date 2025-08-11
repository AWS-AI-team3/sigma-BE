package com.awsgbsa.sigma_BE.user.controller;

import com.awsgbsa.sigma_BE.common.ApiResponse;
import com.awsgbsa.sigma_BE.security.jwt.JwtUtil;
import com.awsgbsa.sigma_BE.user.dto.GoogleLoginRequest;
import com.awsgbsa.sigma_BE.user.dto.LoginResponse;
import com.awsgbsa.sigma_BE.user.service.GoogleOauthService;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/google/login")
    public ResponseEntity<ApiResponse<?>> loginViaGoogle(
            @RequestBody GoogleLoginRequest loginRequest
    ){
        LoginResponse loginResponse = googleOauthService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }

}
