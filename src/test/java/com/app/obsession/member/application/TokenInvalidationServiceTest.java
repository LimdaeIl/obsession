package com.app.obsession.member.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.obsession.global.security.jwt.AccessTokenBlacklistReason;
import com.app.obsession.global.security.jwt.JwtProvider;
import com.app.obsession.global.security.jwt.TokenHashUtil;
import com.app.obsession.member.application.port.AccessTokenBlacklistRepository;
import com.app.obsession.member.application.port.RefreshTokenRepository;
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
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AccessTokenBlacklistRepository accessTokenBlacklistRepository;

    private TokenInvalidationService tokenInvalidationService;

    @BeforeEach
    void setUp() {
        tokenInvalidationService = new TokenInvalidationService(
                jwtProvider,
                tokenHashUtil,
                refreshTokenRepository,
                accessTokenBlacklistRepository
        );
    }

    @Test
    @DisplayName("현재 세션 무효화 시 RT를 삭제하고 AT hash를 blacklist에 저장한다")
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

        verify(refreshTokenRepository).deleteByMemberId(memberId);
        verify(accessTokenBlacklistRepository).saveHash(
                eq(accessTokenHash),
                eq(AccessTokenBlacklistReason.LOGOUT),
                eq(Duration.ofMillis(remainingMillis))
        );
    }

    @Test
    @DisplayName("AT 남은 시간이 없으면 RT만 삭제하고 blacklist에는 저장하지 않는다")
    void invalidateCurrentSession_expiredAccessToken() {
        Long memberId = 1L;
        String accessToken = "expired-access-token";

        when(jwtProvider.getRemainingExpirationMillisFromAccessToken(accessToken))
                .thenReturn(0L);

        tokenInvalidationService.invalidate(
                memberId,
                accessToken,
                AccessTokenBlacklistReason.LOGOUT
        );

        verify(refreshTokenRepository).deleteByMemberId(memberId);
        verify(accessTokenBlacklistRepository, never())
                .saveHash(
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    @DisplayName("AT 남은 시간이 음수면 blacklist에 저장하지 않는다")
    void invalidateCurrentSession_negativeTtl() {
        Long memberId = 1L;
        String accessToken = "access-token";

        when(jwtProvider.getRemainingExpirationMillisFromAccessToken(accessToken))
                .thenReturn(-1L);

        tokenInvalidationService.invalidate(
                memberId,
                accessToken,
                AccessTokenBlacklistReason.LOGOUT
        );

        verify(refreshTokenRepository).deleteByMemberId(memberId);
        verify(accessTokenBlacklistRepository, never())
                .saveHash(any(), any(), any());
    }
}
