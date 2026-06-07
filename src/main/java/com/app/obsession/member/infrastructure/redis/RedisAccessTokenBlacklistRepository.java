package com.app.obsession.member.infrastructure.redis;

import com.app.obsession.global.redis.RedisKey;
import com.app.obsession.global.security.exception.AuthErrorCode;
import com.app.obsession.global.security.exception.AuthException;
import com.app.obsession.global.security.jwt.AccessTokenBlacklistReason;
import com.app.obsession.member.application.port.AccessTokenBlacklistRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisAccessTokenBlacklistRepository implements AccessTokenBlacklistRepository {

    private final StringRedisTemplate redisTemplate;
    private final RedisKey redisKey;

    @Override
    public void saveHash(
            String accessTokenHash,
            AccessTokenBlacklistReason reason,
            Duration ttl
    ) {
        try {
            if (ttl == null) {
                throw new AuthException(AuthErrorCode.TOKEN_STORE_UNAVAILABLE);
            }

            if (ttl.isZero() || ttl.isNegative()) {
                return;
            }

            redisTemplate.opsForValue()
                    .set(redisKey.accessTokenBlacklist(accessTokenHash), reason.name(), ttl);
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            throw new AuthException(AuthErrorCode.TOKEN_STORE_UNAVAILABLE);
        }
    }

    @Override
    public boolean existsByHash(String accessTokenHash) {
        try {
            return Boolean.TRUE.equals(
                    redisTemplate.hasKey(redisKey.accessTokenBlacklist(accessTokenHash))
            );
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            throw new AuthException(AuthErrorCode.TOKEN_STORE_UNAVAILABLE);
        }
    }
}
