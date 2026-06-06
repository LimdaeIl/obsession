package com.app.obsession.member.application;

import com.app.obsession.global.security.jwt.AccessTokenBlacklistReason;
import com.app.obsession.global.security.jwt.JwtProvider;
import com.app.obsession.global.security.jwt.TokenHashUtil;
import com.app.obsession.member.application.port.TokenInvalidationRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenInvalidationService {

    private final JwtProvider jwtProvider;
    private final TokenHashUtil tokenHashUtil;
    private final TokenInvalidationRepository tokenInvalidationRepository;

    public void invalidate(
            Long memberId,
            String accessToken,
            AccessTokenBlacklistReason reason
    ) {
        long remainingExpirationMillis =
                jwtProvider.getRemainingExpirationMillisFromAccessToken(accessToken);

        String accessTokenHash = tokenHashUtil.sha256(accessToken);

        tokenInvalidationRepository.invalidate(
                memberId,
                accessTokenHash,
                reason,
                Duration.ofMillis(Math.max(remainingExpirationMillis, 0))
        );
    }
}

