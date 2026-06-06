package com.app.obsession.global.security.jwt;

import com.app.obsession.global.security.exception.AuthErrorCode;
import com.app.obsession.global.security.exception.AuthException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private final Clock clock;

    private SecretKey secretKey;

    @PostConstruct
    void init() {
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String createAccessToken(JwtClaims claims) {
        Date now = now();
        Date expiration = new Date(now.getTime() + jwtProperties.accessTokenExpirationMillis());

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(claims.memberId()))
                .claim(JwtClaimNames.ROLE, claims.role())
                .claim(JwtClaimNames.TYPE, TokenType.ACCESS.value())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(JwtClaims claims) {
        Date now = now();
        Date expiration = new Date(now.getTime() + jwtProperties.refreshTokenExpirationMillis());

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(claims.memberId()))
                .claim(JwtClaimNames.TYPE, TokenType.REFRESH.value())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public Claims parseAccessToken(String token) {
        Claims claims = parseClaims(
                token,
                AuthErrorCode.EXPIRED_ACCESS_TOKEN,
                AuthErrorCode.INVALID_ACCESS_TOKEN
        );
        validateTokenType(claims, TokenType.ACCESS, AuthErrorCode.INVALID_ACCESS_TOKEN);
        return claims;
    }

    public Claims parseRefreshToken(String token) {
        Claims claims = parseClaims(
                token,
                AuthErrorCode.EXPIRED_REFRESH_TOKEN,
                AuthErrorCode.INVALID_REFRESH_TOKEN
        );
        validateTokenType(claims, TokenType.REFRESH, AuthErrorCode.INVALID_REFRESH_TOKEN);
        return claims;
    }

    public Long getMemberIdFromAccessToken(String token) {
        Claims claims = parseAccessToken(token);
        return parseMemberId(claims, AuthErrorCode.INVALID_ACCESS_TOKEN);
    }

    public Long getMemberIdFromRefreshToken(String token) {
        Claims claims = parseRefreshToken(token);
        return parseMemberId(claims, AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    public String getRoleFromAccessToken(String token) {
        return parseAccessToken(token).get(JwtClaimNames.ROLE, String.class);
    }

    public long getRefreshTokenExpirationMillis() {
        return jwtProperties.refreshTokenExpirationMillis();
    }

    public long getRemainingExpirationMillisFromAccessToken(String token) {
        Date expiration = parseAccessToken(token).getExpiration();
        long remaining = expiration.getTime() - clock.millis();

        return Math.max(remaining, 0);
    }

    public JwtPayload parseAccessPayload(String token) {
        Claims claims = parseAccessToken(token);
        return JwtPayload.from(claims, TokenType.ACCESS, AuthErrorCode.INVALID_ACCESS_TOKEN);
    }

    public JwtPayload parseRefreshPayload(String token) {
        Claims claims = parseRefreshToken(token);
        return JwtPayload.from(claims, TokenType.REFRESH, AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    private Date now() {
        return Date.from(clock.instant());
    }

    private Long parseMemberId(Claims claims, AuthErrorCode invalidErrorCode) {
        try {
            return Long.valueOf(claims.getSubject());
        } catch (RuntimeException e) {
            throw new AuthException(invalidErrorCode);
        }
    }

    private Claims parseClaims(
            String token,
            AuthErrorCode expiredErrorCode,
            AuthErrorCode invalidErrorCode
    ) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new AuthException(expiredErrorCode);
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthException(invalidErrorCode);
        }
    }

    private void validateTokenType(
            Claims claims,
            TokenType expectedType,
            AuthErrorCode errorCode
    ) {
        String actualType = claims.get(JwtClaimNames.TYPE, String.class);

        if (!expectedType.isSame(actualType)) {
            throw new AuthException(errorCode);
        }
    }
}
