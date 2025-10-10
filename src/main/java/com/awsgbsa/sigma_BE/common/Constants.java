package com.awsgbsa.sigma_BE.common;

import java.util.List;

public class Constants {
    private Constants() {
        throw new AssertionError("Cannot instantiate Constants class");
    }

    // 인증필요없는 URL 리스트
    public static List<String> PERMIT_ALL_URLS = List.of(
            "/v1/auth/google/login",
            "/login/oauth2/code/google",
            "/v1/auth/face",
            "/v1/auth/reissue",
            "/h2-console",
            "/h2-console/",
            "/h2-console/**",
            "/favicon.ico",
            "/error",
            "/test",
            "/swagger-ui/index.html",
            "/v3/api-docs",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/v1/auth/google/callback",
            "/actuator/health"
    );

    // jwt 인증헤더
    public static final String AUTHORIZATION_HEADER = "Authorization";
}
