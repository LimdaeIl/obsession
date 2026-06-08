package com.app.obsession.product.application.command;

import com.app.obsession.product.domain.ProductActor;
import com.app.obsession.product.domain.ProductStatus;
import java.math.BigDecimal;

public record UpdateProductCommand(
        Long productId,
        ProductActor actor,
        String name,
        String description,
        BigDecimal price,
        ProductStatus status
) {

}
