package com.app.obsession.member.application.port;

import com.app.obsession.global.security.jwt.AccessTokenBlacklistReason;
import java.time.Duration;

public interface TokenInvalidationRepository {

    void invalidate(
            Long memberId,
            String accessTokenHash,
            AccessTokenBlacklistReason reason,
            Duration accessTokenTtl
    );
}
