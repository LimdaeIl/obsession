package com.app.obsession.member.infrastructure.redis;

import com.app.obsession.global.redis.RedisKey;
import com.app.obsession.global.security.exception.AuthErrorCode;
import com.app.obsession.global.security.exception.AuthException;
import com.app.obsession.member.application.port.RefreshTokenRepository;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenRepository implements RefreshTokenRepository {

    private final StringRedisTemplate redisTemplate;
    private final RedisKey redisKey;

    @Override
    public void saveHash(Long memberId, String refreshTokenHash, Duration ttl) {
        try {
            if (ttl == null || ttl.isZero() || ttl.isNegative()) {
                throw new IllegalArgumentException("Refresh Token TTL은 양수여야 합니다.");
            }

            redisTemplate.opsForValue()
                    .set(redisKey.refreshToken(memberId), refreshTokenHash, ttl);
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            throw new AuthException(AuthErrorCode.TOKEN_STORE_UNAVAILABLE);
        }
    }

    @Override
    public Optional<String> findHashByMemberId(Long memberId) {
        try {
            return Optional.ofNullable(
                    redisTemplate.opsForValue().get(redisKey.refreshToken(memberId))
            );
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            throw new AuthException(AuthErrorCode.TOKEN_STORE_UNAVAILABLE);
        }
    }

    @Override
    public boolean existsByMemberId(Long memberId) {
        try {
            return Boolean.TRUE.equals(
                    redisTemplate.hasKey(redisKey.refreshToken(memberId))
            );
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            throw new AuthException(AuthErrorCode.TOKEN_STORE_UNAVAILABLE);
        }
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        try {
            redisTemplate.delete(redisKey.refreshToken(memberId));
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            throw new AuthException(AuthErrorCode.TOKEN_STORE_UNAVAILABLE);
        }
    }
}
