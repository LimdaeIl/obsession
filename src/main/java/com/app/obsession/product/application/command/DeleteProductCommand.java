package com.app.obsession.product.application.command;

import com.app.obsession.product.domain.ProductActor;

public record DeleteProductCommand(
        Long productId,
        ProductActor actor
) {

}
