package com.app.obsession.member.presentation.dto;

public record LoginResponse(
        Long memberId,
        String accessToken,
        String refreshToken
) {

    public static LoginResponse of(Long memberId, String accessToken, String refreshToken) {
        return new LoginResponse(memberId, accessToken, refreshToken);
    }
}
