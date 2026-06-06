package com.app.obsession.member.infrastructure.redis;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.app.obsession.global.redis.RedisKey;
import com.app.obsession.global.security.exception.AuthErrorCode;
import com.app.obsession.global.security.exception.AuthException;
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
class RedisRefreshTokenRepositoryTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RedisKey redisKey;

    private RedisRefreshTokenRepository repository;

    @BeforeEach
    void setUp() {
        repository = new RedisRefreshTokenRepository(redisTemplate, redisKey);
    }

    @Test
    @DisplayName("RT hash 저장 중 Redis 장애가 발생하면 TOKEN_STORE_UNAVAILABLE 예외")
    void saveHash_redisUnavailable() {
        Long memberId = 1L;
        String key = "obsession:local:auth:refresh-token:1";

        when(redisKey.refreshToken(memberId)).thenReturn(key);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        org.mockito.Mockito.doThrow(new RedisConnectionFailureException("redis down"))
                .when(valueOperations)
                .set(key, "refresh-token-hash", Duration.ofDays(14));

        assertThatThrownBy(() -> repository.saveHash(
                memberId,
                "refresh-token-hash",
                Duration.ofDays(14)
        ))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.TOKEN_STORE_UNAVAILABLE);
    }

    @Test
    @DisplayName("RT hash 조회 중 Redis 장애가 발생하면 TOKEN_STORE_UNAVAILABLE 예외")
    void findHashByMemberId_redisUnavailable() {
        Long memberId = 1L;
        String key = "obsession:local:auth:refresh-token:1";

        when(redisKey.refreshToken(memberId)).thenReturn(key);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key))
                .thenThrow(new RedisConnectionFailureException("redis down"));

        assertThatThrownBy(() -> repository.findHashByMemberId(memberId))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.TOKEN_STORE_UNAVAILABLE);
    }

    @Test
    @DisplayName("RT 존재 여부 확인 중 Redis 장애가 발생하면 TOKEN_STORE_UNAVAILABLE 예외")
    void existsByMemberId_redisUnavailable() {
        Long memberId = 1L;
        String key = "obsession:local:auth:refresh-token:1";

        when(redisKey.refreshToken(memberId)).thenReturn(key);
        when(redisTemplate.hasKey(key))
                .thenThrow(new RedisConnectionFailureException("redis down"));

        assertThatThrownBy(() -> repository.existsByMemberId(memberId))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.TOKEN_STORE_UNAVAILABLE);
    }

    @Test
    @DisplayName("RT 삭제 중 Redis 장애가 발생하면 TOKEN_STORE_UNAVAILABLE 예외")
    void deleteByMemberId_redisUnavailable() {
        Long memberId = 1L;
        String key = "obsession:local:auth:refresh-token:1";

        when(redisKey.refreshToken(memberId)).thenReturn(key);
        when(redisTemplate.delete(key))
                .thenThrow(new RedisConnectionFailureException("redis down"));

        assertThatThrownBy(() -> repository.deleteByMemberId(memberId))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.TOKEN_STORE_UNAVAILABLE);
    }
}
