package com.awsgbsa.sigma_BE.face.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyResultDto {
    private boolean success;
    private boolean match;
    private Double similarity;
    private String message;
}
