package com.app.obsession.product.presentation.dto;

import com.app.obsession.product.application.result.RegisterProductResult;
import com.app.obsession.product.domain.ProductStatus;

import java.math.BigDecimal;

public record RegisterProductResponse(
        Long productId,
        String name,
        BigDecimal price,
        ProductStatus status,
        int initialStock
) {
    public static RegisterProductResponse from(RegisterProductResult result) {
        return new RegisterProductResponse(
                result.productId(),
                result.name(),
                result.price(),
                result.status(),
                result.initialStock()
        );
    }
}
