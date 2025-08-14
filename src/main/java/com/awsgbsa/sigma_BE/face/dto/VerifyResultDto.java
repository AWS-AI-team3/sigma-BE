package com.awsgbsa.sigma_BE.face.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyResultDto {
    private String message;
    private Data data;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class Data {
        private double similarity;

        @JsonProperty("is_same")
        private boolean isSame;
    }

    public boolean isMatch() {
        // null-safe 체크
        return data != null && data.isSame;
    }
}
