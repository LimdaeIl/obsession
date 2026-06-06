package com.app.obsession.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisKey {

    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh-token:";
    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "auth:blacklist:access-token:";

    private final RedisKeyProperties redisKeyProperties;

    public String refreshToken(Long memberId) {
        return prefix() + ":" + REFRESH_TOKEN_PREFIX + memberId;
    }

    public String accessTokenBlacklist(String accessTokenHash) {
        return prefix() + ":" + ACCESS_TOKEN_BLACKLIST_PREFIX + accessTokenHash;
    }

    private String prefix() {
        String keyPrefix = redisKeyProperties.keyPrefix();

        if (keyPrefix == null || keyPrefix.isBlank()) {
            return "obsession:local";
        }

        return keyPrefix;
    }
}
