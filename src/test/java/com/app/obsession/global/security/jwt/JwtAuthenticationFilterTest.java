package com.app.obsession.global.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.obsession.global.security.exception.AuthErrorCode;
import com.app.obsession.global.security.exception.AuthException;
import com.app.obsession.member.application.port.AccessTokenBlacklistRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private BearerTokenResolver bearerTokenResolver;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private AccessTokenBlacklistRepository accessTokenBlacklistRepository;

    @Mock
    private TokenHashUtil tokenHashUtil;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        filter = new JwtAuthenticationFilter(
                bearerTokenResolver,
                jwtProvider,
                accessTokenBlacklistRepository,
                tokenHashUtil
        );
    }

    @Test
    @DisplayName("Authorization Header가 없으면 인증 처리를 하지 않고 다음 필터로 넘긴다")
    void doFilter_noAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("정상 AT면 SecurityContext에 인증 객체를 저장한다")
    void doFilter_validAccessToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String authorizationHeader = "Bearer access-token";
        String accessToken = "access-token";
        String accessTokenHash = "access-token-hash";

        request.addHeader("Authorization", authorizationHeader);

        when(bearerTokenResolver.resolve(authorizationHeader)).thenReturn(accessToken);
        when(tokenHashUtil.sha256(accessToken)).thenReturn(accessTokenHash);
        when(accessTokenBlacklistRepository.existsByHash(accessTokenHash)).thenReturn(false);
        when(jwtProvider.parseAccessPayload(accessToken))
                .thenReturn(new JwtPayload(1L, "CUSTOMER", TokenType.ACCESS));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_CUSTOMER");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("blacklist에 등록된 AT면 BLACKLISTED_ACCESS_TOKEN 예외를 던진다")
    void doFilter_blacklistedAccessToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String authorizationHeader = "Bearer access-token";
        String accessToken = "access-token";
        String accessTokenHash = "access-token-hash";

        request.addHeader("Authorization", authorizationHeader);

        when(bearerTokenResolver.resolve(authorizationHeader)).thenReturn(accessToken);
        when(tokenHashUtil.sha256(accessToken)).thenReturn(accessTokenHash);
        when(accessTokenBlacklistRepository.existsByHash(accessTokenHash)).thenReturn(true);

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.BLACKLISTED_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("Authorization Header 형식이 잘못되면 AuthException을 전파한다")
    void doFilter_invalidBearerHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String authorizationHeader = "Invalid access-token";
        request.addHeader("Authorization", authorizationHeader);

        when(bearerTokenResolver.resolve(authorizationHeader))
                .thenThrow(new AuthException(AuthErrorCode.INVALID_ACCESS_TOKEN));

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("만료된 AT면 EXPIRED_ACCESS_TOKEN 예외를 전파한다")
    void doFilter_expiredAccessToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String authorizationHeader = "Bearer expired-access-token";
        String accessToken = "expired-access-token";
        String accessTokenHash = "expired-access-token-hash";

        request.addHeader("Authorization", authorizationHeader);

        when(bearerTokenResolver.resolve(authorizationHeader)).thenReturn(accessToken);
        when(tokenHashUtil.sha256(accessToken)).thenReturn(accessTokenHash);
        when(accessTokenBlacklistRepository.existsByHash(accessTokenHash)).thenReturn(false);
        when(jwtProvider.parseAccessPayload(accessToken))
                .thenThrow(new AuthException(AuthErrorCode.EXPIRED_ACCESS_TOKEN));

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.EXPIRED_ACCESS_TOKEN);
    }
}
