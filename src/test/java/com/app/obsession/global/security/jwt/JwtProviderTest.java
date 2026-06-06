package com.app.obsession.global.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.app.obsession.global.security.exception.AuthErrorCode;
import com.app.obsession.global.security.exception.AuthException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

    private JwtProvider jwtProvider;
    private Clock clock;


    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
                Instant.parse("2026-01-01T00:00:00Z"),
                ZoneOffset.UTC
        );

        JwtProperties jwtProperties = new JwtProperties(
                "obsession-obsession-obsession-obsession-obsession-secret-key-123456789",
                1_800_000L,
                1_209_600_000L
        );

        jwtProvider = new JwtProvider(jwtProperties, clock);
        jwtProvider.init();
    }

    @Test
    @DisplayName("Access Token을 생성하고 payload를 정상 파싱한다")
    void createAndParseAccessToken() {
        JwtClaims claims = new JwtClaims(1L, "CUSTOMER");

        String accessToken = jwtProvider.createAccessToken(claims);

        JwtPayload payload = jwtProvider.parseAccessPayload(accessToken);

        assertThat(payload.memberId()).isEqualTo(1L);
        assertThat(payload.role()).isEqualTo("CUSTOMER");
        assertThat(payload.tokenType()).isEqualTo(TokenType.ACCESS);
    }

    @Test
    @DisplayName("Refresh Token을 생성하고 payload를 정상 파싱한다")
    void createAndParseRefreshToken() {
        JwtClaims claims = new JwtClaims(1L, "CUSTOMER");

        String refreshToken = jwtProvider.createRefreshToken(claims);

        JwtPayload payload = jwtProvider.parseRefreshPayload(refreshToken);

        assertThat(payload.memberId()).isEqualTo(1L);
        assertThat(payload.tokenType()).isEqualTo(TokenType.REFRESH);
    }

    @Test
    @DisplayName("Access Token을 Refresh Token으로 파싱하면 INVALID_REFRESH_TOKEN 예외")
    void parseAccessTokenAsRefreshToken() {
        JwtClaims claims = new JwtClaims(1L, "CUSTOMER");

        String accessToken = jwtProvider.createAccessToken(claims);

        assertThatThrownBy(() -> jwtProvider.parseRefreshPayload(accessToken))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("Refresh Token을 Access Token으로 파싱하면 INVALID_ACCESS_TOKEN 예외")
    void parseRefreshTokenAsAccessToken() {
        JwtClaims claims = new JwtClaims(1L, "CUSTOMER");

        String refreshToken = jwtProvider.createRefreshToken(claims);

        assertThatThrownBy(() -> jwtProvider.parseAccessPayload(refreshToken))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("위조된 토큰은 INVALID_ACCESS_TOKEN 예외")
    void parseTamperedAccessToken() {
        JwtClaims claims = new JwtClaims(1L, "CUSTOMER");

        String accessToken = jwtProvider.createAccessToken(claims);
        String tamperedToken = accessToken + "tampered";

        assertThatThrownBy(() -> jwtProvider.parseAccessPayload(tamperedToken))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("만료된 Access Token은 EXPIRED_ACCESS_TOKEN 예외")
    void parseExpiredAccessToken() {
        JwtProperties jwtProperties = new JwtProperties(
                "obsession-obsession-obsession-obsession-obsession-secret-key-123456789",
                1L,
                1_209_600_000L
        );

        Clock issuedClock = Clock.fixed(
                Instant.parse("2026-01-01T00:00:00Z"),
                ZoneOffset.UTC
        );

        Clock expiredClock = Clock.fixed(
                Instant.parse("2026-01-01T00:00:01Z"),
                ZoneOffset.UTC
        );

        JwtProvider issuedProvider = new JwtProvider(jwtProperties, issuedClock);
        issuedProvider.init();

        String accessToken = issuedProvider.createAccessToken(new JwtClaims(1L, "CUSTOMER"));

        JwtProvider expiredProvider = new JwtProvider(jwtProperties, expiredClock);
        expiredProvider.init();

        assertThatThrownBy(() -> expiredProvider.parseAccessPayload(accessToken))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.EXPIRED_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("만료된 Refresh Token은 EXPIRED_REFRESH_TOKEN 예외")
    void parseExpiredRefreshToken() {
        JwtProperties jwtProperties = new JwtProperties(
                "obsession-obsession-obsession-obsession-obsession-secret-key-123456789",
                1_800_000L,
                1L
        );

        Clock issuedClock = Clock.fixed(
                Instant.parse("2026-01-01T00:00:00Z"),
                ZoneOffset.UTC
        );

        Clock expiredClock = Clock.fixed(
                Instant.parse("2026-01-01T00:00:01Z"),
                ZoneOffset.UTC
        );

        JwtProvider issuedProvider = new JwtProvider(jwtProperties, issuedClock);
        issuedProvider.init();

        String refreshToken = issuedProvider.createRefreshToken(new JwtClaims(1L, "CUSTOMER"));

        JwtProvider expiredProvider = new JwtProvider(jwtProperties, expiredClock);
        expiredProvider.init();

        assertThatThrownBy(() -> expiredProvider.parseRefreshPayload(refreshToken))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("Access Token의 남은 만료 시간을 계산한다")
    void getRemainingExpirationMillisFromAccessToken() {
        JwtClaims claims = new JwtClaims(1L, "CUSTOMER");

        String accessToken = jwtProvider.createAccessToken(claims);

        long remainingMillis = jwtProvider.getRemainingExpirationMillisFromAccessToken(accessToken);

        assertThat(remainingMillis).isPositive();
        assertThat(remainingMillis).isLessThanOrEqualTo(1_800_000L);
    }
}
