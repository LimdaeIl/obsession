package com.app.obsession.member.infrastructure.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.kakao")
public record KakaoOAuthProperties(
        String clientId,
        String redirectUri
) {

}
