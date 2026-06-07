package com.app.obsession.product.application.command;

import com.app.obsession.product.domain.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductCommand(
        Long memberId,
        String name,
        String description,
        BigDecimal price,
        ProductStatus status,
        int initialStock,
        List<String> imageUrls
) {
}
