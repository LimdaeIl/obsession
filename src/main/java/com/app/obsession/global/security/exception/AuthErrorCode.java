package com.app.obsession.global.security.exception;

import com.app.obsession.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증: 인증이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "인증: 접근 권한이 없습니다."),

    MISSING_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "인증: Access Token이 없습니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "인증: Access Token이 유효하지 않습니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "인증: Access Token이 만료되었습니다."),
    BLACKLISTED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "인증: 로그아웃 처리된 Access Token입니다."),

    MISSING_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "인증: Refresh Token이 없습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "인증: Refresh Token이 유효하지 않습니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "인증: Refresh Token이 만료되었습니다."),

    REUSED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "인증: 재사용된 Refresh Token입니다."),
    TOKEN_STORE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "인증: 토큰 저장소를 사용할 수 없습니다."),
    REFRESH_TOKEN_REPLACED(HttpStatus.UNAUTHORIZED, "인증: 다른 환경에서 로그인되어 Refresh Token이 만료되었습니다.");


    private final HttpStatus httpStatus;
    private final String messageTemplate;

    @Override
    public HttpStatus status() {
        return httpStatus;
    }

    @Override
    public String message() {
        return messageTemplate;
    }
}

