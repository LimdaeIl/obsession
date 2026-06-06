package com.app.obsession.global.security.jwt;

import com.app.obsession.global.security.exception.AuthErrorCode;
import com.app.obsession.global.security.exception.AuthException;
import org.springframework.stereotype.Component;

@Component
public class BearerTokenResolver {

    private static final String BEARER_PREFIX = "Bearer ";

    public String resolve(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new AuthException(AuthErrorCode.MISSING_ACCESS_TOKEN);
        }

        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new AuthException(AuthErrorCode.INVALID_ACCESS_TOKEN);
        }

        String accessToken = authorizationHeader.substring(BEARER_PREFIX.length());

        if (accessToken.isBlank()) {
            throw new AuthException(AuthErrorCode.INVALID_ACCESS_TOKEN);
        }

        return accessToken;
    }
}