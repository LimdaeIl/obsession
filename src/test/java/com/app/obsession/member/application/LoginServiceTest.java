package com.app.obsession.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.obsession.global.security.jwt.JwtClaims;
import com.app.obsession.global.security.jwt.JwtProvider;
import com.app.obsession.global.security.jwt.TokenHashUtil;
import com.app.obsession.member.application.command.LoginCommand;
import com.app.obsession.member.application.port.MemberRepository;
import com.app.obsession.member.application.port.PasswordEncryptor;
import com.app.obsession.member.application.port.RefreshTokenRepository;
import com.app.obsession.member.domain.Member;
import com.app.obsession.member.exception.MemberErrorCode;
import com.app.obsession.member.exception.MemberException;
import com.app.obsession.member.presentation.dto.LoginResponse;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncryptor passwordEncryptor;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenHashUtil tokenHashUtil;

    private LoginService loginService;

    @BeforeEach
    void setUp() {
        loginService = new LoginService(
                memberRepository,
                passwordEncryptor,
                jwtProvider,
                refreshTokenRepository,
                tokenHashUtil
        );
    }

    @Test
    @DisplayName("로그인 성공 시 AT/RT를 발급하고 RT hash를 Redis에 저장한다")
    void login_success() {
        String email = "test@test.com";
        String rawPassword = "1234";
        String encodedPassword = "{bcrypt}encoded-password";
        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        String refreshTokenHash = "refresh-token-hash";

        Member member = Member.createCustomer(
                "홍길동",
                email,
                "01012345678",
                encodedPassword
        );

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(passwordEncryptor.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtProvider.createAccessToken(any(JwtClaims.class))).thenReturn(accessToken);
        when(jwtProvider.createRefreshToken(any(JwtClaims.class))).thenReturn(refreshToken);
        when(tokenHashUtil.sha256(refreshToken)).thenReturn(refreshTokenHash);
        when(jwtProvider.getRefreshTokenExpirationMillis()).thenReturn(1_209_600_000L);

        LoginResponse response = loginService.login(new LoginCommand(email, rawPassword));

        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);

        verify(refreshTokenRepository).saveHash(
                eq(member.getId()),
                eq(refreshTokenHash),
                eq(Duration.ofMillis(1_209_600_000L))
        );
    }

    @Test
    @DisplayName("이메일이 존재하지 않으면 INVALID_LOGIN 예외")
    void login_memberNotFound() {
        String email = "none@test.com";

        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.login(new LoginCommand(email, "1234")))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_LOGIN);

        verify(jwtProvider, never()).createAccessToken(any());
        verify(jwtProvider, never()).createRefreshToken(any());
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 INVALID_LOGIN 예외")
    void login_invalidPassword() {
        String email = "test@test.com";
        String rawPassword = "wrong-password";
        String encodedPassword = "{bcrypt}encoded-password";

        Member member = Member.createCustomer(
                "홍길동",
                email,
                "01012345678",
                encodedPassword
        );

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(passwordEncryptor.matches(rawPassword, encodedPassword)).thenReturn(false);

        assertThatThrownBy(() -> loginService.login(new LoginCommand(email, rawPassword)))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_LOGIN);

        verify(jwtProvider, never()).createAccessToken(any());
        verify(jwtProvider, never()).createRefreshToken(any());
    }

    @Test
    @DisplayName("탈퇴 회원이면 WITHDRAWN_MEMBER 예외")
    void login_withdrawnMember() {
        String email = "test@test.com";

        Member member = Member.createCustomer(
                "홍길동",
                email,
                "01012345678",
                "{bcrypt}encoded-password"
        );
        member.withdraw();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> loginService.login(new LoginCommand(email, "1234")))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.WITHDRAWN_MEMBER);

        verify(jwtProvider, never()).createAccessToken(any());
        verify(jwtProvider, never()).createRefreshToken(any());
    }
}
