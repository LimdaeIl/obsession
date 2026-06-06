package com.app.obsession.global.security.jwt;

import java.util.Arrays;

public enum TokenType {

    ACCESS("access"),
    REFRESH("refresh");

    private final String value;

    TokenType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public boolean isSame(String value) {
        return this.value.equals(value);
    }

    public static TokenType from(String value) {
        return Arrays.stream(values())
                .filter(type -> type.isSame(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 토큰 타입입니다."));
    }
}
