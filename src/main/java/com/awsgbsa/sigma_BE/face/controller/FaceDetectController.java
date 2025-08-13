package com.awsgbsa.sigma_BE.face.controller;

import com.awsgbsa.sigma_BE.common.ApiResponse;
import com.awsgbsa.sigma_BE.exception.CustomException;
import com.awsgbsa.sigma_BE.exception.ErrorCode;
import com.awsgbsa.sigma_BE.face.dto.PresignRequestDto;
import com.awsgbsa.sigma_BE.face.dto.PresignResponseDto;
import com.awsgbsa.sigma_BE.face.dto.ClientVerifyRequestDto;
import com.awsgbsa.sigma_BE.face.dto.VerifyResultDto;
import com.awsgbsa.sigma_BE.face.service.S3Service;
import com.awsgbsa.sigma_BE.face.service.RekognitionService;
import com.awsgbsa.sigma_BE.security.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class FaceDetectController {
    private final JwtUtil jwtUtil;
    private final S3Service s3Service;
    private final RekognitionService rekognitionService;

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

        String objectKey = "register/%d/current.jpg".formatted(userId);
        URL url = s3Service.presignPut(objectKey, presignRequestDto.getContentType());
        PresignResponseDto response = PresignResponseDto.builder()
                .url(url.toString())
                .objectKey(objectKey)
                .contentType(presignRequestDto.getContentType())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/auth/presign")
    @Operation(summary = "얼굴검증 사진 등록 URL발급",
            description = """
                - 요청 필드 contentType: 허용값은 image/jpeg, image/png, image/webp, image/heic
                - 주의: 업로드시 'Content-Type' 헤더 값이 요청한 값과 반드시 같아야 합니다!!
            """)
    public ResponseEntity<ApiResponse<?>> getVerifyUrl(
            @RequestBody @Valid PresignRequestDto presignRequestDto, HttpServletRequest request
    ){
        String token = jwtUtil.resolveToken(request);
        Long userId = jwtUtil.extractUserId(token, false);

        String objectKey = "auth/%d/%s".formatted( userId, UUID.randomUUID().toString());
        URL url = s3Service.presignPut(objectKey, presignRequestDto.getContentType());
        PresignResponseDto response = PresignResponseDto.builder()
                .url(url.toString())
                .objectKey(objectKey)
                .contentType(presignRequestDto.getContentType())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/verify")
    @Operation(summary = "얼굴인증 요청",
            description = "FE에서 S3에 올려둔 인증용(auth) 사진 키를 받아, 해당 유저의 등록용(register) 사진과 비교하여 사용자 인증수행")
    public ResponseEntity<ApiResponse<?>> verfiyValidation(
            @RequestBody @Valid ClientVerifyRequestDto clientVerifyRequestDto, HttpServletRequest request
    ){
        String token = jwtUtil.resolveToken(request);
        Long userId = jwtUtil.extractUserId(token, false);

        // 소유검증 (auth/{해당사용자의 userId}/ 로 시작하는지) -> 크로스유저, 디렉토리 트래버설 방지
        final String authKey = clientVerifyRequestDto.getAuthPhotokey();
        if (authKey == null || authKey.isBlank()
                || authKey.startsWith("/") || authKey.contains("..")
                || !authKey.startsWith("auth/" + userId + "/")) {
            throw new CustomException(ErrorCode.FORBIDDEN_KEY);
        }
        log.info("[Rekognition Verify controller] 소유검증 확인완료");

        // 등록 키
        String registerKey = "register/" + userId + "/current.jpg";

        // 일치여부확인을 위한 rekognition 통신
        VerifyResultDto result;
        try {
            result = rekognitionService.verifyFace(authKey, registerKey);
            log.info("[Rekognition Verify controller] 인증확인 완료");
        } finally {
            // 인증 이미지(auth)는 사용 후 정리 (실패해도 요청 결과는 그대로)
            try {
                s3Service.deleteObject(authKey);
                log.info("[Rekognition Verify controller] Rekognition Verify - 요청사진 삭제완료");
            } catch (Exception e) {
                // 로깅만 하고 무시
                log.warn("[Rekognition Verify controller] 요청사진 삭제실패. key={}", authKey, e);
            }
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
