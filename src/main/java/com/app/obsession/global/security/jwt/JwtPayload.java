package com.app.obsession.global.security.jwt;

import com.app.obsession.global.security.exception.AuthErrorCode;
import com.app.obsession.global.security.exception.AuthException;
import io.jsonwebtoken.Claims;

public record JwtPayload(
        Long memberId,
        String role,
        TokenType tokenType
) {

    public static JwtPayload from(
            Claims claims,
            TokenType tokenType,
            AuthErrorCode invalidErrorCode
    ) {
        try {
            return new JwtPayload(
                    Long.valueOf(claims.getSubject()),
                    claims.get(JwtClaimNames.ROLE, String.class),
                    tokenType
            );
        } catch (RuntimeException e) {
            throw new AuthException(invalidErrorCode);
        }
    }
}

