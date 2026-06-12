package com.app.obsession.member.infrastructure.external;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfoResponse(
        Long id,

        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount,

        Properties properties
) {

    public String email() {
        return kakaoAccount == null ? null : kakaoAccount.email();
    }

    public String nickname() {
        if (kakaoAccount != null
                && kakaoAccount.profile() != null
                && kakaoAccount.profile().nickname() != null) {
            return kakaoAccount.profile().nickname();
        }

        return properties == null ? null : properties.nickname();
    }

    public record KakaoAccount(
            String email,

            @JsonProperty("is_email_valid")
            Boolean isEmailValid,

            @JsonProperty("is_email_verified")
            Boolean isEmailVerified,

            Profile profile
    ) {

    }

    public record Profile(
            String nickname
    ) {

    }

    public record Properties(
            String nickname
    ) {

    }
}
