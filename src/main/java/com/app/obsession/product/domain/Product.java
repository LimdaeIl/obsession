package com.app.obsession.product.domain;

import com.app.obsession.global.entity.BaseAuditEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "v1_products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Product extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_status", nullable = false, length = 30)
    private ProductStatus status;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();


    private Product(String name, String description, BigDecimal price, ProductStatus status) {
        validateName(name);
        validateDescription(description);
        validatePrice(price);
        validateInitialStatus(status);

        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
    }

    public static Product create(String name, String description, BigDecimal price,
            ProductStatus status) {
        return new Product(name, description, price, status);
    }

    public void changeName(String name) {
        validateName(name);
        this.name = name;
    }

    public void changeDescription(String description) {
        validateDescription(description);
        this.description = description;
    }

    public void changePrice(BigDecimal price) {
        validatePrice(price);
        this.price = price;
    }

    public void startSale() {
        if (this.status == ProductStatus.DELETED) {
            throw new IllegalStateException("삭제된 상품은 판매할 수 없습니다.");
        }
        this.status = ProductStatus.ON_SALE;
    }

    public void stopSale() {
        if (this.status == ProductStatus.DELETED) {
            throw new IllegalStateException("삭제된 상품은 판매 중지할 수 없습니다.");
        }
        this.status = ProductStatus.HIDDEN;
    }

    public void markSoldOut() {
        if (this.status != ProductStatus.ON_SALE) {
            throw new IllegalStateException("판매 중인 상품만 품절 처리할 수 있습니다.");
        }
        this.status = ProductStatus.SOLD_OUT;
    }

    public void delete() {
        this.status = ProductStatus.DELETED;
    }

    public void addImage(String imageUrl, int sortOrder) {
        this.images.add(ProductImage.create(this, imageUrl, sortOrder));
    }

    public void removeImage(Long imageId) {
        if (imageId == null) {
            throw new IllegalArgumentException("이미지 ID는 필수입니다.");
        }

        this.images.removeIf(image -> imageId.equals(image.getId()));
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("상품명은 100자를 초과할 수 없습니다.");
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("상품 설명은 필수입니다.");
        }
        if (description.length() > 1000) {
            throw new IllegalArgumentException("상품 설명은 1000자를 초과할 수 없습니다.");
        }
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("상품 가격은 0보다 커야 합니다.");
        }
    }

    private static void validateInitialStatus(ProductStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("상품 상태는 필수입니다.");
        }

        if (!status.canUseAsInitialStatus()) {
            throw new IllegalArgumentException("상품 등록 시 사용할 수 없는 상태입니다.");
        }
    }
}

