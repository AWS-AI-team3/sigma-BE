package com.awsgbsa.sigma_BE.face.service;

import com.awsgbsa.sigma_BE.face.dto.AIVerifyRequestDto;
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

    @Value("${ai.rekognition.verify-url}")
    private String verifyEndpoint;

    @Value("${ai.rekognition.detect-url}")
    private String detectEndpoint;

    public VerifyResultDto verifyFace(String authKey, String registerKey) {
        AIVerifyRequestDto req = AIVerifyRequestDto.builder()
                .src_key(authKey)
                .tgt_key(registerKey)
                .build();
        log.info("[Rekognition Verify Service] src_key={}, tgt_key={}", authKey, registerKey);

        String rawResponse = webClient.post()
                .uri(verifyEndpoint)
                .bodyValue(req)
                .exchangeToMono(response ->
                        response.bodyToMono(String.class)
                                .map(body -> {
                                    log.info("[Rekognition Verify Service] Http Status -- {}", response.statusCode());
                                    log.info("[Rekognition Verify Service] Raw Response -- {}", body);
                                    return body;
                                })
                )
                .timeout(Duration.ofSeconds(5))
                .block();

        try {
            ObjectMapper mapper = new ObjectMapper();
            VerifyResultDto res = mapper.readValue(rawResponse, VerifyResultDto.class);

            if (!res.isSuccess()) {
                throw new RuntimeException("AI 서버 에러: " + rawResponse);
            }

            return res;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("응답 파싱 실패: " + rawResponse, e);
        }
    }
}
