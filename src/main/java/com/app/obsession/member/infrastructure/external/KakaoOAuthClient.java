package com.app.obsession.member.infrastructure.external;

import java.net.URI;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class KakaoOAuthClient {

    private final RestClient restClient;
    private final KakaoOAuthProperties properties;

    public KakaoOAuthClient(
            RestClient.Builder restClientBuilder,
            KakaoOAuthProperties properties
    ) {
        this.restClient = restClientBuilder.build();
        this.properties = properties;
    }

    public KakaoTokenResponse requestToken(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", properties.clientId());
        body.add("redirect_uri", properties.redirectUri());
        body.add("code", code);

        return restClient.post()
                .uri(URI.create("https://kauth.kakao.com/oauth/token"))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(KakaoTokenResponse.class);
    }

    public KakaoUserInfoResponse requestUserInfo(String accessToken) {
        return restClient.get()
                .uri(URI.create("https://kapi.kakao.com/v2/user/me"))
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(KakaoUserInfoResponse.class);
    }
}
