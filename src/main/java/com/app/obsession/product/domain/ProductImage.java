package com.app.obsession.product.domain;

import com.app.obsession.global.entity.BaseAuditEntity;
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
            throw new IllegalArgumentException("상품은 필수입니다.");
        }
    }

    private static void validateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("상품 이미지 URL은 필수입니다.");
        }

        if (imageUrl.length() > 500) {
            throw new IllegalArgumentException("상품 이미지 URL은 500자를 초과할 수 없습니다.");
        }
    }

    private static void validateSortOrder(int sortOrder) {
        if (sortOrder < 0) {
            throw new IllegalArgumentException("이미지 정렬 순서는 0 이상이어야 합니다.");
        }
    }
}
