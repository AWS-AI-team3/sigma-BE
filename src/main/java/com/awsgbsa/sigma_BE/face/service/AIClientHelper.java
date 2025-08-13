package com.awsgbsa.sigma_BE.face.service;

import com.awsgbsa.sigma_BE.exception.CustomException;
import com.awsgbsa.sigma_BE.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
@Slf4j
public class AIClientHelper {
    private final ObjectMapper objectMapper;

    // 정상응답(응답코드 2XX일때)에 대한 파싱처리, DTO로 변환
    public <T> Mono<T> parseResponseBody(String body, Class<T> clazz, Predicate<T> successCheck) {
        try {
            // JSON 문자열을 DTO 객체로 변환
            T res = objectMapper.readValue(body, clazz);

            // 응답 객체의 성공 여부 체크
//            if (!successCheck.test(res)) {
//                throw new CustomException(ErrorCode.AI_PROCESSING_FAILED);
//            }
            return Mono.just(res);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.PARSE_ERROR);
        }
    }

    // 에러상황(응답코드 4xx, 5xx)에 대한 에러상태코드별 처리
    public CustomException mapToCustomException(HttpStatusCode status, String body) {
        return switch (status.value()) {
            case 400 -> new CustomException(ErrorCode.INVALID_REQUEST);
            case 404 -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
            case 500 -> {
                log.error("AI 서버 내부 오류: {}", body);
                yield new CustomException(ErrorCode.AI_SERVER_ERROR);
            }
            default -> new CustomException(ErrorCode.UNKNOWN_ERROR);
        };
    }
}
