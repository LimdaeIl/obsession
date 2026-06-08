package com.app.obsession.product.domain;

import com.app.obsession.global.entity.BaseAuditEntity;
import com.app.obsession.product.exception.ProductErrorCode;
import com.app.obsession.product.exception.ProductException;
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

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

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

    private Product(
            Long sellerId,
            String name,
            String description,
            BigDecimal price,
            ProductStatus status
    ) {
        validateSellerId(sellerId);
        validateName(name);
        validateDescription(description);
        validatePrice(price);
        validateInitialStatus(status);

        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
    }

    public static Product create(
            Long sellerId,
            String name,
            String description,
            BigDecimal price,
            ProductStatus status
    ) {
        return new Product(sellerId, name, description, price, status);
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

    public void changeStatus(ProductStatus status) {
        if (this.status == ProductStatus.DELETED) {
            throw new ProductException(ProductErrorCode.DELETED_PRODUCT_CANNOT_BE_UPDATED);
        }

        if (status == null) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_STATUS);
        }

        this.status = status;
    }

    public void startSale() {
        if (this.status == ProductStatus.DELETED) {
            throw new ProductException(ProductErrorCode.DELETED_PRODUCT_CANNOT_BE_SOLD);
        }

        this.status = ProductStatus.ON_SALE;
    }

    public void stopSale() {
        if (this.status == ProductStatus.DELETED) {
            throw new ProductException(ProductErrorCode.DELETED_PRODUCT_CANNOT_STOP_SALE);
        }

        this.status = ProductStatus.HIDDEN;
    }

    public void markSoldOut() {
        if (this.status != ProductStatus.ON_SALE) {
            throw new ProductException(ProductErrorCode.ONLY_ON_SALE_PRODUCT_CAN_BE_SOLD_OUT);
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
            throw new ProductException(ProductErrorCode.INVALID_IMAGE_ID);
        }

        this.images.removeIf(image -> imageId.equals(image.getId()));
    }

    public boolean isOwnedBy(Long memberId) {
        return memberId != null && this.sellerId.equals(memberId);
    }

    private static void validateSellerId(Long sellerId) {
        if (sellerId == null || sellerId <= 0) {
            throw new ProductException(ProductErrorCode.INVALID_SELLER_ID);
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_NAME);
        }

        if (name.length() > 100) {
            throw new ProductException(ProductErrorCode.PRODUCT_NAME_TOO_LONG);
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_DESCRIPTION);
        }

        if (description.length() > 1000) {
            throw new ProductException(ProductErrorCode.PRODUCT_DESCRIPTION_TOO_LONG);
        }
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_PRICE);
        }
    }

    private static void validateInitialStatus(ProductStatus status) {
        if (status == null) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_STATUS);
        }

        if (!status.canUseAsInitialStatus()) {
            throw new ProductException(ProductErrorCode.INVALID_INITIAL_PRODUCT_STATUS);
        }
    }
}
