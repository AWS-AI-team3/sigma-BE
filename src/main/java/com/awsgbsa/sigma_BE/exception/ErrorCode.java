package com.awsgbsa.sigma_BE.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Spring security 인증 & 인가 실패
    INVALID_AUTENTICATION(HttpStatus.UNAUTHORIZED, "인증이 실패하였습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근권한이 없습니다."),

    // JWT 유효성 검증관련 에러
    ACCESS_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "AccessToken이 요청에 포함되어있지 않습니다."),
    EMPTY_TOKEN_ERROR(HttpStatus.BAD_REQUEST, "인증토큰이 비어있습니다."),
    INVALID_TOKEN_SIGNATURE(HttpStatus.FORBIDDEN, "유효하지 않은 토큰입니다. - jwt 서명검증 실패"),
    MALFORMED_TOKEN_ERROR(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다. - 잘못된 jwt형식"),
    UNSUPPORTED_TOKEN_ERROR(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다. - 지원하지 않는 jwt"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다. - 만료된 토큰"),
    TOKEN_PARSING_FAILED(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다. - 파싱실패"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Refresh Token이 만료되었거나 유효하지 않습니다. 다시 로그인 해주세요."),
    MISMATCHED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "저장된 RefreshToken과 일치하지 않습니다."),
    REDIS_SAVE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "리프레시 토큰 저장에 실패했습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    DUPLICATE_MOTION_UPDATE(HttpStatus.BAD_REQUEST, "동일 모션을 여러 컨트롤에 중복지정할 수 없습니다."),
    USER_SETTINGS_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자의 모션 세팅정보를 불러올 수 없습니다."),
    INVALID_MOTION_VALUE(HttpStatus.BAD_REQUEST, "유효하지 않은 모션값 설정입니다."),
    USER_SETTINGS_FAIL(HttpStatus.BAD_REQUEST, "사용자 설정변경에 실패하였습니다."),

    FORBIDDEN_KEY(HttpStatus.BAD_REQUEST, "유효하지 않은 사진접근key 요청입니다."),

    AI_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "[AI처리 오류] AI 서버 처리내용 - 실패"),
    PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "응답 파싱 실패" ),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "[AI처리 오류] 잘못된 요청"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "[AI처리 오류] 대상 이미지를 찾을 수 없음"),
    AI_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "[AI처리 오류] AI 서버 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "[AI처리 오류] 예상치 못한 오류"),
    INVALID_FACE(HttpStatus.BAD_REQUEST, "[AI처리 오류] 인증에 등록가능한 유효한 얼굴사진이 아닙니다."),
    MISMATCH_FACE(HttpStatus.UNAUTHORIZED, "[AI처리 오류] 얼굴인증에 실패하였습니다."),

    FACE_NOT_REGISTERED(HttpStatus.UNAUTHORIZED, "얼굴인증을 등록하지 않은 상태입니다."),
    FACE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "얼굴인증이 아직 진행되지 않았습니다."),
    FACE_MISTMATCH(HttpStatus.UNAUTHORIZED, "얼굴인증에 실패하였습니다.(결과처리=false)"),


    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버내부 문제가 발생했습니다." )
    ;

    private final HttpStatus status;
    private final String message;
}
