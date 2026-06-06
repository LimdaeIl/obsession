package com.app.obsession.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.obsession.global.security.exception.AuthErrorCode;
import com.app.obsession.global.security.exception.AuthException;
import com.app.obsession.global.security.jwt.JwtClaims;
import com.app.obsession.global.security.jwt.JwtPayload;
import com.app.obsession.global.security.jwt.JwtProvider;
import com.app.obsession.global.security.jwt.TokenHashUtil;
import com.app.obsession.global.security.jwt.TokenType;
import com.app.obsession.member.application.port.MemberRepository;
import com.app.obsession.member.application.port.RefreshTokenRepository;
import com.app.obsession.member.domain.Member;
import com.app.obsession.member.exception.MemberErrorCode;
import com.app.obsession.member.exception.MemberException;
import com.app.obsession.member.presentation.dto.TokenResponse;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class ReissueTokenServiceTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TokenHashUtil tokenHashUtil;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private ReissueTokenService reissueTokenService;

    @BeforeEach
    void setUp() {
        reissueTokenService = new ReissueTokenService(
                jwtProvider,
                tokenHashUtil,
                memberRepository,
                refreshTokenRepository
        );
    }

    @Test
    @DisplayName("Refresh Token이 유효하고 Redis hash와 일치하면 새 AT/RT를 발급하고 새 RT hash를 저장한다")
    void reissue_success() {
        Long memberId = 1L;
        String oldRefreshToken = "old-refresh-token";
        String oldRefreshTokenHash = "old-refresh-token-hash";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        String newRefreshTokenHash = "new-refresh-token-hash";

        Member member = Member.createCustomer(
                "홍길동",
                "test@test.com",
                "01012345678",
                "{bcrypt}encoded-password"
        );

        JwtPayload payload = new JwtPayload(memberId, "CUSTOMER", TokenType.REFRESH);

        when(jwtProvider.parseRefreshPayload(oldRefreshToken)).thenReturn(payload);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(refreshTokenRepository.findHashByMemberId(memberId))
                .thenReturn(Optional.of(oldRefreshTokenHash));
        when(tokenHashUtil.sha256(oldRefreshToken)).thenReturn(oldRefreshTokenHash);
        when(jwtProvider.createAccessToken(any(JwtClaims.class))).thenReturn(newAccessToken);
        when(jwtProvider.createRefreshToken(any(JwtClaims.class))).thenReturn(newRefreshToken);
        when(tokenHashUtil.sha256(newRefreshToken)).thenReturn(newRefreshTokenHash);
        when(jwtProvider.getRefreshTokenExpirationMillis()).thenReturn(1_209_600_000L);

        TokenResponse response = reissueTokenService.reissue(oldRefreshToken);

        assertThat(response.accessToken()).isEqualTo(newAccessToken);
        assertThat(response.refreshToken()).isEqualTo(newRefreshToken);

        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);

        verify(refreshTokenRepository).saveHash(
                eq(memberId),
                eq(newRefreshTokenHash),
                ttlCaptor.capture()
        );

        assertThat(ttlCaptor.getValue()).isEqualTo(Duration.ofMillis(1_209_600_000L));
    }

    @Test
    @DisplayName("요청 RT hash가 Redis 저장 hash와 다르면 RT를 삭제하고 REUSED_REFRESH_TOKEN 예외를 던진다")
    void reissue_reusedRefreshToken() {
        Long memberId = 1L;
        String refreshToken = "reused-refresh-token";
        String savedHash = "saved-refresh-token-hash";
        String requestHash = "request-refresh-token-hash";

        Member member = Member.createCustomer(
                "홍길동",
                "test@test.com",
                "01012345678",
                "{bcrypt}encoded-password"
        );

        JwtPayload payload = new JwtPayload(memberId, "CUSTOMER", TokenType.REFRESH);

        when(jwtProvider.parseRefreshPayload(refreshToken)).thenReturn(payload);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(refreshTokenRepository.findHashByMemberId(memberId)).thenReturn(Optional.of(savedHash));
        when(tokenHashUtil.sha256(refreshToken)).thenReturn(requestHash);

        assertThatThrownBy(() -> reissueTokenService.reissue(refreshToken))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.REUSED_REFRESH_TOKEN);

        verify(refreshTokenRepository).deleteByMemberId(memberId);
    }

    @Test
    @DisplayName("Redis에 저장된 RT hash가 없으면 INVALID_REFRESH_TOKEN 예외를 던진다")
    void reissue_refreshTokenNotFoundInRedis() {
        Long memberId = 1L;
        String refreshToken = "refresh-token";

        JwtPayload payload = new JwtPayload(memberId, "CUSTOMER", TokenType.REFRESH);

        when(jwtProvider.parseRefreshPayload(refreshToken)).thenReturn(payload);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(
                Member.createCustomer("홍길동", "test@test.com", "01012345678", "{bcrypt}password")
        ));
        when(refreshTokenRepository.findHashByMemberId(memberId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reissueTokenService.reissue(refreshToken))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("탈퇴 회원이 reissue 요청하면 RT를 삭제하고 WITHDRAWN_MEMBER 예외를 던진다")
    void reissue_withdrawnMember() {
        Long memberId = 1L;
        String refreshToken = "refresh-token";
        String refreshTokenHash = "refresh-token-hash";

        Member member = Member.createCustomer("홍길동", "test@test.com", "01012345678", "{bcrypt}password");
        member.withdraw();

        JwtPayload payload = new JwtPayload(memberId, "CUSTOMER", TokenType.REFRESH);

        when(jwtProvider.parseRefreshPayload(refreshToken)).thenReturn(payload);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(refreshTokenRepository.findHashByMemberId(memberId)).thenReturn(Optional.of(refreshTokenHash));
        when(tokenHashUtil.sha256(refreshToken)).thenReturn(refreshTokenHash);

        assertThatThrownBy(() -> reissueTokenService.reissue(refreshToken))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.WITHDRAWN_MEMBER);

        verify(refreshTokenRepository).deleteByMemberId(memberId);
    }

    @Test
    @DisplayName("RT는 유효하지만 회원이 없으면 MEMBER_NOT_FOUND 예외를 던진다")
    void reissue_memberNotFound() {
        Long memberId = 999L;
        String refreshToken = "refresh-token";

        JwtPayload payload = new JwtPayload(memberId, "CUSTOMER", TokenType.REFRESH);

        when(jwtProvider.parseRefreshPayload(refreshToken)).thenReturn(payload);
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reissueTokenService.reissue(refreshToken))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("탈퇴 회원이 reissue 요청하면 새 토큰을 발급하지 않는다")
    void reissue_withdrawnMember_doesNotIssueNewTokens() {
        Long memberId = 1L;
        String refreshToken = "refresh-token";
        String refreshTokenHash = "refresh-token-hash";

        Member member = Member.createCustomer("홍길동", "test@test.com", "01012345678", "{bcrypt}password");
        member.withdraw();

        JwtPayload payload = new JwtPayload(memberId, "CUSTOMER", TokenType.REFRESH);

        when(jwtProvider.parseRefreshPayload(refreshToken)).thenReturn(payload);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(refreshTokenRepository.findHashByMemberId(memberId)).thenReturn(Optional.of(refreshTokenHash));
        when(tokenHashUtil.sha256(refreshToken)).thenReturn(refreshTokenHash);

        assertThatThrownBy(() -> reissueTokenService.reissue(refreshToken))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.WITHDRAWN_MEMBER);

        verify(jwtProvider, never()).createAccessToken(any());
        verify(jwtProvider, never()).createRefreshToken(any());
    }

    @Test
    @DisplayName("재사용된 RT 요청이면 새 토큰을 발급하지 않는다")
    void reissue_reusedRefreshToken_doesNotIssueNewTokens() {
        Long memberId = 1L;
        String refreshToken = "reused-refresh-token";
        String savedHash = "saved-refresh-token-hash";
        String requestHash = "request-refresh-token-hash";

        Member member = Member.createCustomer("홍길동", "test@test.com", "01012345678", "{bcrypt}password");

        JwtPayload payload = new JwtPayload(memberId, "CUSTOMER", TokenType.REFRESH);

        when(jwtProvider.parseRefreshPayload(refreshToken)).thenReturn(payload);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(refreshTokenRepository.findHashByMemberId(memberId)).thenReturn(Optional.of(savedHash));
        when(tokenHashUtil.sha256(refreshToken)).thenReturn(requestHash);

        assertThatThrownBy(() -> reissueTokenService.reissue(refreshToken))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.REUSED_REFRESH_TOKEN);

        verify(jwtProvider, never()).createAccessToken(any());
        verify(jwtProvider, never()).createRefreshToken(any());
    }

    @Test
    @DisplayName("Redis에 RT hash가 없으면 새 토큰을 발급하지 않는다")
    void reissue_missingRedisRefreshToken_doesNotIssueNewTokens() {
        Long memberId = 1L;
        String refreshToken = "refresh-token";

        Member member = Member.createCustomer("홍길동", "test@test.com", "01012345678", "{bcrypt}password");
        JwtPayload payload = new JwtPayload(memberId, "CUSTOMER", TokenType.REFRESH);

        when(jwtProvider.parseRefreshPayload(refreshToken)).thenReturn(payload);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(refreshTokenRepository.findHashByMemberId(memberId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reissueTokenService.reissue(refreshToken))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);

        verify(jwtProvider, never()).createAccessToken(any());
        verify(jwtProvider, never()).createRefreshToken(any());
    }

    @Test
    @DisplayName("reissue 성공 시 기존 RT hash가 아니라 새 RT hash를 저장한다")
    void reissue_success_savesNewRefreshTokenHash() {
        Long memberId = 1L;
        String oldRefreshToken = "old-refresh-token";
        String oldRefreshTokenHash = "old-refresh-token-hash";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        String newRefreshTokenHash = "new-refresh-token-hash";

        Member member = Member.createCustomer("홍길동", "test@test.com", "01012345678", "{bcrypt}password");

        JwtPayload payload = new JwtPayload(memberId, "CUSTOMER", TokenType.REFRESH);

        when(jwtProvider.parseRefreshPayload(oldRefreshToken)).thenReturn(payload);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(refreshTokenRepository.findHashByMemberId(memberId)).thenReturn(Optional.of(oldRefreshTokenHash));
        when(tokenHashUtil.sha256(oldRefreshToken)).thenReturn(oldRefreshTokenHash);
        when(jwtProvider.createAccessToken(any(JwtClaims.class))).thenReturn(newAccessToken);
        when(jwtProvider.createRefreshToken(any(JwtClaims.class))).thenReturn(newRefreshToken);
        when(tokenHashUtil.sha256(newRefreshToken)).thenReturn(newRefreshTokenHash);
        when(jwtProvider.getRefreshTokenExpirationMillis()).thenReturn(1_209_600_000L);

        reissueTokenService.reissue(oldRefreshToken);

        verify(refreshTokenRepository).saveHash(
                eq(memberId),
                eq(newRefreshTokenHash),
                eq(Duration.ofMillis(1_209_600_000L))
        );
    }


}
