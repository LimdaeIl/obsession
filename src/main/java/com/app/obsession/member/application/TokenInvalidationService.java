package com.app.obsession.member.application;

import com.app.obsession.global.security.jwt.AccessTokenBlacklistReason;
import com.app.obsession.global.security.jwt.JwtProvider;
import com.app.obsession.global.security.jwt.TokenHashUtil;
import com.app.obsession.member.application.port.AccessTokenBlacklistRepository;
import com.app.obsession.member.application.port.RefreshTokenRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenInvalidationService {

    private final JwtProvider jwtProvider;
    private final TokenHashUtil tokenHashUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;

    public void invalidate(
            Long memberId,
            String accessToken,
            AccessTokenBlacklistReason reason
    ) {

        refreshTokenRepository.deleteByMemberId(memberId);

        long remainingExpirationMillis =
                jwtProvider.getRemainingExpirationMillisFromAccessToken(accessToken);

        if (remainingExpirationMillis <= 0) {
            return;
        }

        String accessTokenHash =
                tokenHashUtil.sha256(accessToken);

        accessTokenBlacklistRepository.saveHash(
                accessTokenHash,
                reason,
                Duration.ofMillis(remainingExpirationMillis)
        );
    }
}
