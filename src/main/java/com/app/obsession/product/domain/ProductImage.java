package com.app.obsession.product.domain;

import com.app.obsession.global.entity.BaseAuditEntity;
import com.app.obsession.product.exception.ProductErrorCode;
import com.app.obsession.product.exception.ProductException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "v1_product_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ProductImage extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    private ProductImage(Product product, String imageUrl, int sortOrder) {
        validateProduct(product);
        validateImageUrl(imageUrl);
        validateSortOrder(sortOrder);

        this.product = product;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    public static ProductImage create(Product product, String imageUrl, int sortOrder) {
        return new ProductImage(product, imageUrl, sortOrder);
    }

    private static void validateProduct(Product product) {
        if (product == null) {
            throw new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    private static void validateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new ProductException(ProductErrorCode.INVALID_IMAGE_URL);
        }

        if (imageUrl.length() > 500) {
            throw new ProductException(ProductErrorCode.PRODUCT_IMAGE_URL_TOO_LONG);
        }
    }

    private static void validateSortOrder(int sortOrder) {
        if (sortOrder < 0) {
            throw new ProductException(ProductErrorCode.INVALID_IMAGE_SORT_ORDER);
        }
    }
}
