package com.app.obsession.global.idempotency;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum IdempotencyStatus {
    PROCESSING("진행중"),
    COMPLETED("완료"),
    FAILED("실패");


    private final String description;
}
