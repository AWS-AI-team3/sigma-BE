package com.awsgbsa.sigma_BE.face.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectResultDto {
    private String message;
    private Data data;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Data {
        @JsonProperty("is_face")
        private boolean isFace;
        @JsonProperty("face_count")
        private int faceCount;
    }
}
