package com.app.obsession.member.application.result;

public record LoginResult(
        Long memberId,
        String accessToken,
        String refreshToken
) {

}
