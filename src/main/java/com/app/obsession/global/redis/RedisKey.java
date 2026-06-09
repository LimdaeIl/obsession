package com.app.obsession.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisKey {

    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh-token:";
    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "auth:blacklist:access-token:";
    private static final String PRODUCT_STOCK_LOCK_PREFIX = "lock:product-stock:";

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
            // 사용자 요청 처리 중 발생하는 비즈니스 예외나 인증 예외가 아니기 때문에 RuntimeException을 던집니다.
            throw new IllegalStateException("redis keyPrefix 공백 또는 null일 수 없습니다.");
        }

        return keyPrefix;
    }

    public String productStockLock(Long productId) {
        return prefix() + ":" + PRODUCT_STOCK_LOCK_PREFIX + productId;
    }
}
