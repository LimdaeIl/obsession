package com.app.obsession.order.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    CREATED("주문 생성"),
    PAID("결제 완료"),
    CANCELED("주문 취소"),
    FAILED("주문 실패");

    private final String description;
}
