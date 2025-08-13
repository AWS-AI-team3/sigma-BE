package com.awsgbsa.sigma_BE.face.service;

import com.awsgbsa.sigma_BE.face.dto.AIDetectRequestDto;
import com.awsgbsa.sigma_BE.face.dto.AIVerifyRequestDto;
import com.awsgbsa.sigma_BE.face.dto.DetectResultDto;
import com.awsgbsa.sigma_BE.face.dto.VerifyResultDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3Client;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RekognitionService {

    private final WebClient webClient;
    private final AIClientHelper aiClientHelper;

    @Value("${ai.rekognition.verify-url}")
    private String verifyEndpoint;

    @Value("${ai.rekognition.detect-url}")
    private String detectEndpoint;

    // 얼굴감지
    public DetectResultDto detectFace(String targetKey){
        AIDetectRequestDto req = AIDetectRequestDto.builder()
                .key(targetKey).build();
        log.info("[Rekognition Detect Service] key={}", targetKey);

        return callAiServer(detectEndpoint, req, DetectResultDto.class);
    }

    // 얼굴검증
    public VerifyResultDto verifyFace(String authKey, String registerKey) {
        AIVerifyRequestDto req = AIVerifyRequestDto.builder()
                .src_key(authKey)
                .tgt_key(registerKey)
                .build();
        log.info("[Rekognition Verify Service] src_key={}, tgt_key={}", authKey, registerKey);

        return callAiServer(verifyEndpoint, req, VerifyResultDto.class);
    }

    // 공통모델호출 메서드
    private <T> T callAiServer(String uri, Object requestDto, Class<T> responseClass) {
        return webClient.post()
                .uri(uri)
                .bodyValue(requestDto)
                .exchangeToMono(response -> {
                    HttpStatusCode status = response.statusCode();
                    return response.bodyToMono(String.class)
                            .flatMap(body -> {
                                log.info("[AI Raw Response] {}", body);

                                if (status.is2xxSuccessful()) {
                                    // 정상 응답 → DTO 변환 (success=false도 그대로 반환)
                                    return aiClientHelper.parseResponseBody(body, responseClass, res -> true);
                                } else {
                                    // 에러 응답 → CustomException 변환
                                    return Mono.error(aiClientHelper.mapToCustomException(status, body));
                                }
                            });
                })
                .block();
    }
}
