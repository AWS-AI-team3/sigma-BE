package com.awsgbsa.sigma_BE.face.controller;

import com.awsgbsa.sigma_BE.common.ApiResponse;
import com.awsgbsa.sigma_BE.exception.CustomException;
import com.awsgbsa.sigma_BE.exception.ErrorCode;
import com.awsgbsa.sigma_BE.face.dto.*;
import com.awsgbsa.sigma_BE.face.service.FaceSessionService;
import com.awsgbsa.sigma_BE.face.service.S3Service;
import com.awsgbsa.sigma_BE.face.service.RekognitionService;
import com.awsgbsa.sigma_BE.security.jwt.JwtUtil;
import com.awsgbsa.sigma_BE.user.service.UserService;
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
public class FaceAuthController {
    private final JwtUtil jwtUtil;
    private final S3Service s3Service;
    private final RekognitionService rekognitionService;
    private final FaceSessionService faceSessionService;
    private final UserService userService;

    @PostMapping("/register/presign")
    @Operation(summary = "얼굴인증 사진 등록 URL발급 (1)",
            description = """
                - 요청 필드 contentType: 허용값은 image/jpeg, image/png, image/webp, image/heic
                - 주의: 업로드시 'Content-Type' 헤더 값이 요청한 값과 반드시 같아야 합니다!!
            """)
    public ResponseEntity<ApiResponse<?>> getRegisterUrl(
            @RequestBody @Valid PresignRequestDto presignRequestDto, HttpServletRequest request
    ){
        String token = jwtUtil.resolveToken(request);
        Long userId = jwtUtil.extractUserId(token, false);

        String objectKey = "register/%d/current".formatted(userId);
        URL url = s3Service.presignPut(objectKey, presignRequestDto.getContentType());
        PresignResponseDto response = PresignResponseDto.builder()
                .url(url.toString())
                .objectKey(objectKey)
                .contentType(presignRequestDto.getContentType())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/auth/presign")
    @Operation(summary = "얼굴검증 사진 등록 URL발급 (3)",
            description = """
                - 요청 필드 contentType: 허용값은 image/jpeg, image/png, image/webp, image/heic
                - 주의: 업로드시 'Content-Type' 헤더 값이 요청한 값과 반드시 같아야 합니다!!
            """)
    public ResponseEntity<ApiResponse<?>> getVerifyUrl(
            @RequestBody @Valid PresignRequestDto presignRequestDto, HttpServletRequest request
    ){
        String token = jwtUtil.resolveToken(request);
        Long userId = jwtUtil.extractUserId(token, false);

        // 등록 여부 검사
        if (!userService.findByUserId(userId).isFaceRegistered()) {
            throw new CustomException(ErrorCode.FACE_NOT_REGISTERED);
        }

        String objectKey = "auth/%d/%s".formatted( userId, UUID.randomUUID().toString());
        URL url = s3Service.presignPut(objectKey, presignRequestDto.getContentType());
        PresignResponseDto response = PresignResponseDto.builder()
                .url(url.toString())
                .objectKey(objectKey)
                .contentType(presignRequestDto.getContentType())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/register/complete")
    @Operation(summary = "얼굴 등록 완료 처리 (2)",
            description = "등록 사진 업로드 후 호출하면 detect 검증 후 등록 완료(사용자의 기본 경로 key : /register/{userID}/current.jpg )")
    public ResponseEntity<ApiResponse<?>> completeRegister(
            HttpServletRequest request
    ) {
        Long userId = jwtUtil.extractUserId(jwtUtil.resolveToken(request), false);

        String registerKey = "register/" + userId + "/current";

        // Detect 작업
        DetectResultDto detectResult = rekognitionService.detectFace(registerKey);
        if (!detectResult.getData().isFace()) {
            // 실패 시 사진 삭제
            s3Service.deleteObject(registerKey);
            throw new CustomException(ErrorCode.INVALID_FACE);
        }

        // 사용자 얼굴등록상태로 업데이트!!
        userService.updateFaceRegistered(userId);
        return ResponseEntity.ok(ApiResponse.success("얼굴인증 등록완료"));
    }

    @PostMapping("/auth/complete")
    @Operation(summary = "얼굴 인증 완료 처리 (4)",
            description = "인증 사진 업로드 후 호출하면 detect → verify(기존 인증사진 & 전달키의 사진) → 삭제 절차 수행")
    public ResponseEntity<ApiResponse<?>> completeAuth(
            @RequestBody @Valid ClientVerifyRequestDto requestDto, HttpServletRequest request
    ) {
        Long userId = jwtUtil.extractUserId(jwtUtil.resolveToken(request), false);
        String authKey = requestDto.getAuthPhotokey();
        String registerKey = "register/" + userId + "/current";

        // 소유검증
        if (authKey == null || !authKey.startsWith("auth/" + userId + "/")) {
            throw new CustomException(ErrorCode.FORBIDDEN_KEY);
        }

        // 1. Detect 작업
        DetectResultDto detectResult = rekognitionService.detectFace(authKey);
        if (!detectResult.getData().isFace()) {
            s3Service.deleteObject(authKey);
            throw new CustomException(ErrorCode.INVALID_FACE);
        }

        // 2. Verify 작업
        VerifyResultDto verifyResult;
        try {
            verifyResult = rekognitionService.verifyFace(authKey, registerKey);
        } finally {
            // 항상 auth 이미지 삭제
            s3Service.deleteObject(authKey);
        }

        if(verifyResult.isMatch()){
            // facesession token발급
            faceSessionService.createSession(userId);
            return ResponseEntity.ok(ApiResponse.success("얼굴인증 완료, 세션발급 완료"));
        } else {
            throw new CustomException(ErrorCode.FACE_MISTMATCH);
        }
    }

    @PostMapping("/session/check")
    @Operation(summary = "얼굴 인증 여부 확인 (5)",
            description = "해당 유저아이디로 발급된 faceSession이 redis에 존재하는지 확인하여 얼굴인증확인여부를 판단")
    public ResponseEntity<ApiResponse<?>> checkSession(HttpServletRequest request) {
        Long userId = jwtUtil.extractUserId(jwtUtil.resolveToken(request), false);
        if(faceSessionService.validateSession(userId)){
            return ResponseEntity.ok(ApiResponse.success("인증확인이 완료된 사용자입니다."));
        } else {
            throw new CustomException(ErrorCode.FACE_UNAUTHORIZED);
        }
    }

}

