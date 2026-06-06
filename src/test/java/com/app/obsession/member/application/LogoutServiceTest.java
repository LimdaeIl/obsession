package com.app.obsession.member.application;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.obsession.global.security.jwt.AccessTokenBlacklistReason;
import com.app.obsession.global.security.jwt.BearerTokenResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private BearerTokenResolver bearerTokenResolver;

    @Mock
    private TokenInvalidationService tokenInvalidationService;

    private LogoutService logoutService;

    @BeforeEach
    void setUp() {
        logoutService = new LogoutService(
                bearerTokenResolver,
                tokenInvalidationService
        );
    }

    @Test
    @DisplayName("로그아웃 시 Authorization Header에서 AT를 추출하고 현재 세션을 무효화한다")
    void logout_success() {
        Long memberId = 1L;
        String authorizationHeader = "Bearer access-token";
        String accessToken = "access-token";

        when(bearerTokenResolver.resolve(authorizationHeader)).thenReturn(accessToken);

        logoutService.logout(memberId, authorizationHeader);

        verify(tokenInvalidationService).invalidate(
                memberId,
                accessToken,
                AccessTokenBlacklistReason.LOGOUT
        );
    }
}
