package com.awsgbsa.sigma_BE.face.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientVerifyRequestDto {
    @NotBlank(message = "authPhotokey는 필수입니다.")
    @Schema(description = "S3 사진 키", example = "auth/{userId}/8b1a9953-...-abcd")
    private String authPhotokey;
}
