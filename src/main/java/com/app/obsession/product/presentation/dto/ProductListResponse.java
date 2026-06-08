package com.app.obsession.product.presentation.dto;

import com.app.obsession.product.domain.Product;
import com.app.obsession.product.domain.ProductStatus;
import java.math.BigDecimal;

public record ProductListResponse(
        Long productId,
        String name,
        String description,
        BigDecimal price,
        ProductStatus status
) {

    public static ProductListResponse from(Product product) {
        return new ProductListResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStatus());
    }

}
