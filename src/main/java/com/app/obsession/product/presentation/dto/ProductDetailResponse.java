package com.app.obsession.product.presentation.dto;


import com.app.obsession.product.domain.Product;
import com.app.obsession.product.domain.ProductImage;
import com.app.obsession.product.domain.ProductStatus;
import java.math.BigDecimal;
import java.util.List;

public record ProductDetailResponse(
        Long productId,
        String name,
        String description,
        BigDecimal price,
        ProductStatus status,
        List<ProductImageResponse> images
) {

    public static ProductDetailResponse from(Product product) {
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStatus(),
                product.getImages()
                        .stream()
                        .map(ProductImageResponse::from)
                        .toList()
        );
    }

    public record ProductImageResponse(
            Long imageId,
            String imageUrl,
            int sortOrder
    ) {

        public static ProductImageResponse from(ProductImage image) {
            return new ProductImageResponse(
                    image.getId(),
                    image.getImageUrl(),
                    image.getSortOrder()
            );
        }
    }

}
