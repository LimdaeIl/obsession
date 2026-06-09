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
import com.app.obsession.member.application.result.ReissueTokenResult;
import com.app.obsession.member.domain.Member;
import com.app.obsession.member.exception.MemberErrorCode;
import com.app.obsession.member.exception.MemberException;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @DisplayName("Refresh Token이 유효하고 저장소 회전에 성공하면 새 AT/RT를 발급한다")
    void reissue_success() {
        Long memberId = 1L;
        String oldRefreshToken = "old-refresh-token";
        String oldRefreshTokenHash = "old-refresh-token-hash";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        String newRefreshTokenHash = "new-refresh-token-hash";

        Member member = createMember();
        JwtPayload payload = new JwtPayload(memberId, "CUSTOMER", TokenType.REFRESH);

        when(jwtProvider.parseRefreshPayload(oldRefreshToken)).thenReturn(payload);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(jwtProvider.createAccessToken(any(JwtClaims.class))).thenReturn(newAccessToken);
        when(jwtProvider.createRefreshToken(any(JwtClaims.class))).thenReturn(newRefreshToken);
        when(tokenHashUtil.sha256(oldRefreshToken)).thenReturn(oldRefreshTokenHash);
        when(tokenHashUtil.sha256(newRefreshToken)).thenReturn(newRefreshTokenHash);
        when(jwtProvider.getRefreshTokenExpirationMillis()).thenReturn(1_209_600_000L);
        when(refreshTokenRepository.rotateIfMatches(
                eq(memberId),
                eq(oldRefreshTokenHash),
                eq(newRefreshTokenHash),
                eq(Duration.ofMillis(1_209_600_000L))
        )).thenReturn(true);

        ReissueTokenResult result = reissueTokenService.reissue(oldRefreshToken);

        assertThat(result.accessToken()).isEqualTo(newAccessToken);
        assertThat(result.refreshToken()).isEqualTo(newRefreshToken);

        verify(refreshTokenRepository).rotateIfMatches(
                eq(memberId),
                eq(oldRefreshTokenHash),
                eq(newRefreshTokenHash),
                eq(Duration.ofMillis(1_209_600_000L))
        );
    }

    @Test
    @DisplayName("Refresh Token 회전에 실패하면 RT를 삭제하고 REUSED_REFRESH_TOKEN 예외를 던진다")
    void reissue_reusedRefreshToken() {
        Long memberId = 1L;
        String refreshToken = "reused-refresh-token";
        String requestHash = "request-refresh-token-hash";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        String newRefreshTokenHash = "new-refresh-token-hash";

        JwtPayload payload = new JwtPayload(memberId, "CUSTOMER", TokenType.REFRESH);

        when(jwtProvider.parseRefreshPayload(refreshToken)).thenReturn(payload);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(createMember()));
        when(jwtProvider.createAccessToken(any(JwtClaims.class))).thenReturn(newAccessToken);
        when(jwtProvider.createRefreshToken(any(JwtClaims.class))).thenReturn(newRefreshToken);
        when(tokenHashUtil.sha256(refreshToken)).thenReturn(requestHash);
        when(tokenHashUtil.sha256(newRefreshToken)).thenReturn(newRefreshTokenHash);
        when(jwtProvider.getRefreshTokenExpirationMillis()).thenReturn(1_209_600_000L);
        when(refreshTokenRepository.rotateIfMatches(
                eq(memberId),
                eq(requestHash),
                eq(newRefreshTokenHash),
                eq(Duration.ofMillis(1_209_600_000L))
        )).thenReturn(false);

        assertThatThrownBy(() -> reissueTokenService.reissue(refreshToken))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.REUSED_REFRESH_TOKEN);

        verify(refreshTokenRepository).deleteByMemberId(memberId);
    }

    @Test
    @DisplayName("Redis에 RT hash가 없으면 회전 실패로 보고 REUSED_REFRESH_TOKEN 예외를 던진다")
    void reissue_refreshTokenNotFoundInRedis() {
        Long memberId = 1L;
        String refreshToken = "refresh-token";
        String refreshTokenHash = "refresh-token-hash";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        String newRefreshTokenHash = "new-refresh-token-hash";

        JwtPayload payload = new JwtPayload(memberId, "CUSTOMER", TokenType.REFRESH);

        when(jwtProvider.parseRefreshPayload(refreshToken)).thenReturn(payload);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(createMember()));
        when(jwtProvider.createAccessToken(any(JwtClaims.class))).thenReturn(newAccessToken);
        when(jwtProvider.createRefreshToken(any(JwtClaims.class))).thenReturn(newRefreshToken);
        when(tokenHashUtil.sha256(refreshToken)).thenReturn(refreshTokenHash);
        when(tokenHashUtil.sha256(newRefreshToken)).thenReturn(newRefreshTokenHash);
        when(jwtProvider.getRefreshTokenExpirationMillis()).thenReturn(1_209_600_000L);
        when(refreshTokenRepository.rotateIfMatches(
                eq(memberId),
                eq(refreshTokenHash),
                eq(newRefreshTokenHash),
                eq(Duration.ofMillis(1_209_600_000L))
        )).thenReturn(false);

        assertThatThrownBy(() -> reissueTokenService.reissue(refreshToken))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.REUSED_REFRESH_TOKEN);

        verify(refreshTokenRepository).deleteByMemberId(memberId);
    }

    @Test
    @DisplayName("탈퇴 회원이 reissue 요청하면 RT를 삭제하고 WITHDRAWN_MEMBER 예외를 던진다")
    void reissue_withdrawnMember() {
        Long memberId = 1L;
        String refreshToken = "refresh-token";

        Member member = createMember();
        member.withdraw();

        JwtPayload payload = new JwtPayload(memberId, "CUSTOMER", TokenType.REFRESH);

        when(jwtProvider.parseRefreshPayload(refreshToken)).thenReturn(payload);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> reissueTokenService.reissue(refreshToken))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.WITHDRAWN_MEMBER);

        verify(refreshTokenRepository).deleteByMemberId(memberId);
        verify(jwtProvider, never()).createAccessToken(any());
        verify(jwtProvider, never()).createRefreshToken(any());
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
    @DisplayName("회전 실패 시 새 토큰은 응답하지 않고 예외를 던진다")
    void reissue_rotateFailed_doesNotReturnTokens() {
        Long memberId = 1L;
        String refreshToken = "refresh-token";
        String refreshTokenHash = "refresh-token-hash";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        String newRefreshTokenHash = "new-refresh-token-hash";

        JwtPayload payload = new JwtPayload(memberId, "CUSTOMER", TokenType.REFRESH);

        when(jwtProvider.parseRefreshPayload(refreshToken)).thenReturn(payload);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(createMember()));
        when(jwtProvider.createAccessToken(any(JwtClaims.class))).thenReturn(newAccessToken);
        when(jwtProvider.createRefreshToken(any(JwtClaims.class))).thenReturn(newRefreshToken);
        when(tokenHashUtil.sha256(refreshToken)).thenReturn(refreshTokenHash);
        when(tokenHashUtil.sha256(newRefreshToken)).thenReturn(newRefreshTokenHash);
        when(jwtProvider.getRefreshTokenExpirationMillis()).thenReturn(1_209_600_000L);
        when(refreshTokenRepository.rotateIfMatches(
                eq(memberId),
                eq(refreshTokenHash),
                eq(newRefreshTokenHash),
                eq(Duration.ofMillis(1_209_600_000L))
        )).thenReturn(false);

        assertThatThrownBy(() -> reissueTokenService.reissue(refreshToken))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.REUSED_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("reissue 성공 시 기존 RT hash와 새 RT hash로 원자적 회전을 요청한다")
    void reissue_success_rotatesRefreshTokenHash() {
        Long memberId = 1L;
        String oldRefreshToken = "old-refresh-token";
        String oldRefreshTokenHash = "old-refresh-token-hash";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        String newRefreshTokenHash = "new-refresh-token-hash";
        Duration ttl = Duration.ofMillis(1_209_600_000L);

        JwtPayload payload = new JwtPayload(memberId, "CUSTOMER", TokenType.REFRESH);

        when(jwtProvider.parseRefreshPayload(oldRefreshToken)).thenReturn(payload);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(createMember()));
        when(jwtProvider.createAccessToken(any(JwtClaims.class))).thenReturn(newAccessToken);
        when(jwtProvider.createRefreshToken(any(JwtClaims.class))).thenReturn(newRefreshToken);
        when(tokenHashUtil.sha256(oldRefreshToken)).thenReturn(oldRefreshTokenHash);
        when(tokenHashUtil.sha256(newRefreshToken)).thenReturn(newRefreshTokenHash);
        when(jwtProvider.getRefreshTokenExpirationMillis()).thenReturn(ttl.toMillis());
        when(refreshTokenRepository.rotateIfMatches(
                eq(memberId),
                eq(oldRefreshTokenHash),
                eq(newRefreshTokenHash),
                eq(ttl)
        )).thenReturn(true);

        reissueTokenService.reissue(oldRefreshToken);

        verify(refreshTokenRepository).rotateIfMatches(
                eq(memberId),
                eq(oldRefreshTokenHash),
                eq(newRefreshTokenHash),
                eq(ttl)
        );
    }

    private Member createMember() {
        return Member.createCustomer(
                "홍길동",
                "test@test.com",
                "01012345678",
                "{bcrypt}password"
        );
    }
}
