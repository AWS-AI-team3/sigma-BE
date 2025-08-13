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
    private boolean success;
    @JsonProperty("is_face")
    private boolean isFace;
    private int count;
    private String message;
}
