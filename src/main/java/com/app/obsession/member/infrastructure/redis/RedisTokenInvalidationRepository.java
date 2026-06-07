package com.app.obsession.member.infrastructure.redis;

import com.app.obsession.global.redis.RedisKey;
import com.app.obsession.global.security.exception.AuthErrorCode;
import com.app.obsession.global.security.exception.AuthException;
import com.app.obsession.global.security.jwt.AccessTokenBlacklistReason;
import com.app.obsession.member.application.port.TokenInvalidationRepository;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisTokenInvalidationRepository implements TokenInvalidationRepository {

    private final StringRedisTemplate redisTemplate;
    private final RedisKey redisKey;

    @Override
    public void invalidate(
            Long memberId,
            String accessTokenHash,
            AccessTokenBlacklistReason reason,
            Duration accessTokenTtl
    ) {
        try {
            String refreshTokenKey = redisKey.refreshToken(memberId);
            String accessTokenBlacklistKey = redisKey.accessTokenBlacklist(accessTokenHash);

            String script = """
                    redis.call('DEL', KEYS[1])
                    
                    local ttl = tonumber(ARGV[2])
                    if ttl > 0 then
                        redis.call('SET', KEYS[2], ARGV[1], 'PX', ttl)
                    end
                    
                    return 1
                    """;

            redisTemplate.execute(
                    new DefaultRedisScript<>(script, Long.class),
                    List.of(refreshTokenKey, accessTokenBlacklistKey),
                    reason.name(),
                    String.valueOf(accessTokenTtl.toMillis())
            );
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            throw new AuthException(AuthErrorCode.TOKEN_STORE_UNAVAILABLE);
        }
    }
}
