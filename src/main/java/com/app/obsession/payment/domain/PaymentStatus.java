package com.app.obsession.payment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaymentStatus {
    READY("결제 대기"),
    APPROVED("결제 승인"),
    FAILED("결제 실패"),
    CANCELED("결제 취소");

    private final String description;
}
