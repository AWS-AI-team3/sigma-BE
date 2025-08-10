package com.awsgbsa.sigma_BE.common;

import java.util.List;

public class Constants {
    private Constants() {
        throw new AssertionError("Cannot instantiate Constants class");
    }

    // 인증필요없는 URL 리스트
    public static List<String> PERMIT_ALL_URLS = List.of(
            "/v1/auth/login",
            "/v1/auth/face",
            "/v1/auth/reissue",
            "/h2-console",
            "/h2-console/",
            "/h2-console/**",
            "/favicon.ico",
            "/error",
            "/test",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    );
}
