package com.app.obsession.member.application.port;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenRepository {

    void saveHash(Long memberId, String refreshTokenHash, Duration ttl);

    Optional<String> findHashByMemberId(Long memberId);

    boolean existsByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);
}
