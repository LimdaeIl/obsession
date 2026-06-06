package com.app.obsession.member.presentation.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {

    public static TokenResponse of(String accessToken, String refreshToken) {
        return new TokenResponse(accessToken, refreshToken);
    }
}

