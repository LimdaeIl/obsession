package com.app.obsession.member.presentation.dto;

public record SignupResponse(
        Long memberId
) {

    public static SignupResponse of(Long memberId) {
        return new SignupResponse(memberId);
    }
}
