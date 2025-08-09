package com.awsgbsa.sigma_BE.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean isSucess;
    private T data;
    private ApiError error;

    public static<T> ApiResponse<T> success(T data){
        return ApiResponse.<T>builder()
                .isSucess(true)
                .data(data)
                .build();
    }

    public static<T> ApiResponse<T> fail(T data, String code, String message){
        return ApiResponse.<T>builder()
                .isSucess(false)
                .data(data)
                .error(new ApiError(code, message))
                .build();
    }
}
