package com.app.obsession.product.application.command;

import com.app.obsession.product.domain.ProductActor;
import com.app.obsession.product.domain.ProductStatus;
import java.math.BigDecimal;
import java.util.List;

public record CreateProductCommand(
        ProductActor actor,
        String name,
        String description,
        BigDecimal price,
        ProductStatus status,
        int initialStock,
        List<String> imageUrls
) {
}
