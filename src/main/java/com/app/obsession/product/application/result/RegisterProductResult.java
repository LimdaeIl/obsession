package com.app.obsession.product.application.result;

import com.app.obsession.product.domain.ProductStatus;

import java.math.BigDecimal;

public record RegisterProductResult(
        Long productId,
        String name,
        BigDecimal price,
        ProductStatus status,
        int initialStock
) {
}
