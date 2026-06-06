package com.app.obsession.member.infrastructure.redis;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.app.obsession.global.redis.RedisKey;
import com.app.obsession.global.security.exception.AuthErrorCode;
import com.app.obsession.global.security.exception.AuthException;
import com.app.obsession.global.security.jwt.AccessTokenBlacklistReason;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisAccessTokenBlacklistRepositoryTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RedisKey redisKey;

    private RedisAccessTokenBlacklistRepository repository;

    @BeforeEach
    void setUp() {
        repository = new RedisAccessTokenBlacklistRepository(redisTemplate, redisKey);
    }

    @Test
    @DisplayName("AT blacklist 저장 중 Redis 장애가 발생하면 TOKEN_STORE_UNAVAILABLE 예외")
    void saveHash_redisUnavailable() {
        String accessTokenHash = "access-token-hash";
        String key = "obsession:local:auth:blacklist:access-token:access-token-hash";

        when(redisKey.accessTokenBlacklist(accessTokenHash)).thenReturn(key);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        org.mockito.Mockito.doThrow(new RedisConnectionFailureException("redis down"))
                .when(valueOperations)
                .set(key, AccessTokenBlacklistReason.LOGOUT.name(), Duration.ofMinutes(30));

        assertThatThrownBy(() -> repository.saveHash(
                accessTokenHash,
                AccessTokenBlacklistReason.LOGOUT,
                Duration.ofMinutes(30)
        ))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.TOKEN_STORE_UNAVAILABLE);
    }

    @Test
    @DisplayName("AT blacklist 조회 중 Redis 장애가 발생하면 TOKEN_STORE_UNAVAILABLE 예외")
    void existsByHash_redisUnavailable() {
        String accessTokenHash = "access-token-hash";
        String key = "obsession:local:auth:blacklist:access-token:access-token-hash";

        when(redisKey.accessTokenBlacklist(accessTokenHash)).thenReturn(key);
        when(redisTemplate.hasKey(key))
                .thenThrow(new RedisConnectionFailureException("redis down"));

        assertThatThrownBy(() -> repository.existsByHash(accessTokenHash))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.TOKEN_STORE_UNAVAILABLE);
    }
}
