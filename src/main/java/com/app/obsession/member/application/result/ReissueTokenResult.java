package com.app.obsession.member.application.result;

public record ReissueTokenResult(
        String accessToken,
        String refreshToken
) {

}
