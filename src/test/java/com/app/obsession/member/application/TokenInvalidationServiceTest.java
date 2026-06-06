package com.app.obsession.member.application;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.obsession.global.security.jwt.AccessTokenBlacklistReason;
import com.app.obsession.global.security.jwt.JwtProvider;
import com.app.obsession.global.security.jwt.TokenHashUtil;
import com.app.obsession.member.application.port.TokenInvalidationRepository;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenInvalidationServiceTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TokenHashUtil tokenHashUtil;

    @Mock
    private TokenInvalidationRepository tokenInvalidationRepository;

    private TokenInvalidationService tokenInvalidationService;

    @BeforeEach
    void setUp() {
        tokenInvalidationService = new TokenInvalidationService(
                jwtProvider,
                tokenHashUtil,
                tokenInvalidationRepository
        );
    }

    @Test
    @DisplayName("현재 세션 무효화 시 RT 삭제와 AT blacklist 저장을 저장소에 위임한다")
    void invalidateCurrentSession_success() {
        Long memberId = 1L;
        String accessToken = "access-token";
        String accessTokenHash = "access-token-hash";
        long remainingMillis = 1_000L;

        when(jwtProvider.getRemainingExpirationMillisFromAccessToken(accessToken))
                .thenReturn(remainingMillis);
        when(tokenHashUtil.sha256(accessToken)).thenReturn(accessTokenHash);

        tokenInvalidationService.invalidate(
                memberId,
                accessToken,
                AccessTokenBlacklistReason.LOGOUT
        );

        verify(tokenInvalidationRepository).invalidate(
                memberId,
                accessTokenHash,
                AccessTokenBlacklistReason.LOGOUT,
                Duration.ofMillis(remainingMillis)
        );
    }

    @Test
    @DisplayName("AT 남은 시간이 없으면 TTL 0으로 저장소에 무효화를 위임한다")
    void invalidateCurrentSession_expiredAccessToken() {
        Long memberId = 1L;
        String accessToken = "expired-access-token";
        String accessTokenHash = "expired-access-token-hash";

        when(jwtProvider.getRemainingExpirationMillisFromAccessToken(accessToken))
                .thenReturn(0L);
        when(tokenHashUtil.sha256(accessToken)).thenReturn(accessTokenHash);

        tokenInvalidationService.invalidate(
                memberId,
                accessToken,
                AccessTokenBlacklistReason.LOGOUT
        );

        verify(tokenInvalidationRepository).invalidate(
                memberId,
                accessTokenHash,
                AccessTokenBlacklistReason.LOGOUT,
                Duration.ZERO
        );
    }

    @Test
    @DisplayName("AT 남은 시간이 음수면 TTL 0으로 저장소에 무효화를 위임한다")
    void invalidateCurrentSession_negativeTtl() {
        Long memberId = 1L;
        String accessToken = "access-token";
        String accessTokenHash = "access-token-hash";

        when(jwtProvider.getRemainingExpirationMillisFromAccessToken(accessToken))
                .thenReturn(-1L);
        when(tokenHashUtil.sha256(accessToken)).thenReturn(accessTokenHash);

        tokenInvalidationService.invalidate(
                memberId,
                accessToken,
                AccessTokenBlacklistReason.LOGOUT
        );

        verify(tokenInvalidationRepository).invalidate(
                memberId,
                accessTokenHash,
                AccessTokenBlacklistReason.LOGOUT,
                Duration.ZERO
        );
    }
}
