package com.app.obsession.member.application.port;

import com.app.obsession.global.security.jwt.AccessTokenBlacklistReason;
import java.time.Duration;

public interface AccessTokenBlacklistRepository {

    void saveHash(
            String accessTokenHash,
            AccessTokenBlacklistReason reason,
            Duration ttl
    );

    boolean existsByHash(String accessTokenHash);
}
