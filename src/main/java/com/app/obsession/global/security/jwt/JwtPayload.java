package com.app.obsession.global.security.jwt;

import io.jsonwebtoken.Claims;

public record JwtPayload(
        Long memberId,
        String role,
        TokenType tokenType
) {

    public static JwtPayload from(Claims claims) {
        return new JwtPayload(
                Long.valueOf(claims.getSubject()),
                claims.get("role", String.class),
                TokenType.from(claims.get("type", String.class))
        );
    }
}

