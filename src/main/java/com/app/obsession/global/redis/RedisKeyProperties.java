package com.app.obsession.global.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.redis")
public record RedisKeyProperties(
        String keyPrefix
) {
}