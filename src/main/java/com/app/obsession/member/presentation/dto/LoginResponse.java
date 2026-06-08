package com.app.obsession.member.presentation.dto;

public record LoginResponse(
        Long memberId,
        String accessToken) {

    public static LoginResponse of(Long memberId, String accessToken) {
        return new LoginResponse(memberId, accessToken);
    }
}
