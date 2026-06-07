package com.app.obsession.product.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ProductStatus {
    DRAFT("임시저장"),
    HIDDEN("비공개"),
    DELETED("삭제"),
    ON_SALE("판매중"),
    SOLD_OUT("품절");

    private final String description;

    public boolean canDisplay() {
        return this == ON_SALE || this == SOLD_OUT;
    }

    public boolean canSell() {
        return this == ON_SALE;
    }

    public boolean canUseAsInitialStatus() {
        return this == DRAFT || this == ON_SALE || this == HIDDEN;
    }

}
