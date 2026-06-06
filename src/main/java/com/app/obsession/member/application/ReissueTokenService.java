package com.app.obsession.member.application;

import com.app.obsession.global.security.exception.AuthErrorCode;
import com.app.obsession.global.security.exception.AuthException;
import com.app.obsession.global.security.jwt.JwtClaims;
import com.app.obsession.global.security.jwt.JwtPayload;
import com.app.obsession.global.security.jwt.JwtProvider;
import com.app.obsession.global.security.jwt.TokenHashUtil;
import com.app.obsession.member.application.port.MemberRepository;
import com.app.obsession.member.application.port.RefreshTokenRepository;
import com.app.obsession.member.domain.Member;
import com.app.obsession.member.exception.MemberErrorCode;
import com.app.obsession.member.exception.MemberException;
import com.app.obsession.member.presentation.dto.TokenResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j(topic = "ReissueTokenService")
@Service
@RequiredArgsConstructor
public class ReissueTokenService {

    private final JwtProvider jwtProvider;
    private final TokenHashUtil tokenHashUtil;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(readOnly = true)
    public TokenResponse reissue(String refreshToken) {
        validateRefreshToken(refreshToken);

        JwtPayload payload = jwtProvider.parseRefreshPayload(refreshToken);
        Long memberId = payload.memberId();

        Member member = getMember(memberId);

        validateStoredRefreshToken(memberId, refreshToken);
        validateMemberStatus(memberId, member);

        JwtClaims jwtClaims = JwtClaims.of(member);

        String newAccessToken = jwtProvider.createAccessToken(jwtClaims);
        String newRefreshToken = jwtProvider.createRefreshToken(jwtClaims);

        String newRefreshTokenHash = tokenHashUtil.sha256(newRefreshToken);

        refreshTokenRepository.saveHash(
                memberId,
                newRefreshTokenHash,
                Duration.ofMillis(jwtProvider.getRefreshTokenExpirationMillis())
        );

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }

    private void validateRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException(AuthErrorCode.MISSING_REFRESH_TOKEN);
        }
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateStoredRefreshToken(Long memberId, String refreshToken) {
        String savedRefreshTokenHash = refreshTokenRepository.findHashByMemberId(memberId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        String refreshTokenHash = tokenHashUtil.sha256(refreshToken);

        if (!savedRefreshTokenHash.equals(refreshTokenHash)) {
            log.warn("Refresh token reuse detected. memberId={}", memberId);

            refreshTokenRepository.deleteByMemberId(memberId);

            throw new AuthException(AuthErrorCode.REUSED_REFRESH_TOKEN);
        }
    }

    private void validateMemberStatus(Long memberId, Member member) {
        if (member.isWithdrawn()) {
            refreshTokenRepository.deleteByMemberId(memberId);
            throw new MemberException(MemberErrorCode.WITHDRAWN_MEMBER);
        }
    }
}
