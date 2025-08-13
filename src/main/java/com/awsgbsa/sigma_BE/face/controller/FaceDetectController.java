package com.awsgbsa.sigma_BE.face.controller;

import com.awsgbsa.sigma_BE.common.ApiResponse;
import com.awsgbsa.sigma_BE.face.dto.PresignRequestDto;
import com.awsgbsa.sigma_BE.face.dto.PresignResponseDto;
import com.awsgbsa.sigma_BE.face.service.PresignService;
import com.awsgbsa.sigma_BE.security.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;
import java.util.UUID;

@Tag(name = "얼굴 인증", description = "얼굴 인증관련 API")
@RestController
@RequestMapping("/v1/faces")
@RequiredArgsConstructor
public class FaceDetectController {
    private final JwtUtil jwtUtil;
    private final PresignService presignService;

    @PostMapping("/register/presign")
    @Operation(summary = "얼굴인증 사진 등록 URL발급",
            description = """
                - 요청 필드 contentType: 허용값은 image/jpeg, image/png, image/webp, image/heic
                - 주의: 업로드시 'Content-Type' 헤더 값이 요청한 값과 반드시 같아야 합니다!!
            """)
    public ResponseEntity<ApiResponse<?>> getRegisterUrl(
            @RequestBody @Valid PresignRequestDto presignRequestDto, HttpServletRequest request
    ){
        String token = jwtUtil.resolveToken(request);
        Long userId = jwtUtil.extractUserId(token, false);

        String objectKey = "register/%d/%s".formatted( userId, UUID.randomUUID().toString());
        URL url = presignService.presignPut(objectKey, presignRequestDto.getContentType());
        PresignResponseDto response = PresignResponseDto.builder()
                .url(url.toString())
                .objectKey(objectKey)
                .contentType(presignRequestDto.getContentType())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
