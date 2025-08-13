package com.awsgbsa.sigma_BE.face.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class PresignRequestDto {
    @NotBlank(message = "contentType은 필수입니다.")
    @Pattern(
            regexp = "image/(jpeg|png|webp|heic)",
            message = "허용되지 않은 contentType 입니다. (image/jpeg, image/png, image/webp, image/heic)"
    )
    @Schema(
            description = "허용되는 이미지 MIME 타입",
            allowableValues = {"image/jpeg", "image/png", "image/webp", "image/heic"},
            example = "image/jpeg"
    )
    private String contentType;
}
