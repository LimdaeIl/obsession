package com.app.obsession.member.exception;

import com.app.obsession.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "회원: 이미 사용 중인 이메일입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원: 회원을 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "회원: 비밀번호가 올바르지 않습니다."),
    WITHDRAWN_MEMBER(HttpStatus.BAD_REQUEST, "회원: 탈퇴한 회원입니다."),

    INVALID_MEMBER_NAME(HttpStatus.BAD_REQUEST, "회원: 이름은 필수입니다."),
    INVALID_MEMBER_EMAIL(HttpStatus.BAD_REQUEST, "회원: 이메일은 필수입니다."),
    INVALID_MEMBER_PHONE(HttpStatus.BAD_REQUEST, "회원: 전화번호는 필수입니다."),
    INVALID_MEMBER_PASSWORD(HttpStatus.BAD_REQUEST, "회원: 비밀번호는 필수입니다."),
    INVALID_COMPANY_NAME(HttpStatus.BAD_REQUEST, "회원: 회사명은 필수입니다."),
    INVALID_COMPANY_BRN(HttpStatus.BAD_REQUEST, "회원: 사업자등록번호는 필수입니다."),
    INVALID_SOCIAL_MEMBER_ID(HttpStatus.BAD_REQUEST, "회원: 회원 ID는 필수입니다."),
    INVALID_SOCIAL_PROVIDER(HttpStatus.BAD_REQUEST, "회원: 소셜 Provider는 필수입니다."),
    INVALID_SOCIAL_PROVIDER_ID(HttpStatus.BAD_REQUEST, "회원: 소셜 Provider ID는 필수입니다."),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "회원: 이메일 또는 비밀번호가 올바르지 않습니다."),
    SOCIAL_ONLY_MEMBER(HttpStatus.BAD_REQUEST, "회원: 소셜 로그인 전용 회원입니다."),;


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

