package com.awsgbsa.sigma_BE.face.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PresignResponseDto {
    private String url; // PUT presigned URL
    private String objectKey; // 업로드 완료 후 서버에 알려줄 키
    private String contentType;      // 클라가 그대로 써야하는 업로드 객체타입
}
