package com.app.obsession.product.domain;

import com.app.obsession.global.entity.BaseAuditEntity;
import com.app.obsession.product.exception.ProductErrorCode;
import com.app.obsession.product.exception.ProductException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "v1_product_stocks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ProductStock extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Column(name = "sold_quantity", nullable = false)
    private int soldQuantity;

    @Version
    private Long version;

    private ProductStock(Long productId, int totalQuantity) {
        validateProductId(productId);
        validateQuantity(totalQuantity);

        this.productId = productId;
        this.totalQuantity = totalQuantity;
        this.reservedQuantity = 0;
        this.soldQuantity = 0;
    }

    public static ProductStock create(Long productId, int totalQuantity) {
        return new ProductStock(productId, totalQuantity);
    }

    public int availableQuantity() {
        return totalQuantity - reservedQuantity - soldQuantity;
    }

    public boolean hasAvailableQuantity(int quantity) {
        return availableQuantity() >= quantity;
    }

    public void increase(int quantity) {
        validatePositiveQuantity(quantity);
        this.totalQuantity += quantity;
    }

    public void decrease(int quantity) {
        validatePositiveQuantity(quantity);

        if (availableQuantity() < quantity) {
            throw new ProductException(ProductErrorCode.CANNOT_DECREASE_MORE_THAN_AVAILABLE_STOCK);
        }

        this.totalQuantity -= quantity;
    }

    public void reserve(int quantity) {
        validatePositiveQuantity(quantity);

        if (!hasAvailableQuantity(quantity)) {
            throw new ProductException(ProductErrorCode.INSUFFICIENT_STOCK);
        }

        this.reservedQuantity += quantity;
    }

    public void release(int quantity) {
        validatePositiveQuantity(quantity);

        if (this.reservedQuantity < quantity) {
            throw new ProductException(ProductErrorCode.CANNOT_RELEASE_MORE_THAN_RESERVED_STOCK);
        }

        this.reservedQuantity -= quantity;
    }

    public void confirm(int quantity) {
        validatePositiveQuantity(quantity);

        if (this.reservedQuantity < quantity) {
            throw new ProductException(ProductErrorCode.CANNOT_CONFIRM_MORE_THAN_RESERVED_STOCK);
        }

        this.reservedQuantity -= quantity;
        this.soldQuantity += quantity;
    }

    public void restoreSold(int quantity) {
        validatePositiveQuantity(quantity);

        if (this.soldQuantity < quantity) {
            throw new ProductException(ProductErrorCode.CANNOT_RESTORE_MORE_THAN_SOLD_STOCK);
        }

        this.soldQuantity -= quantity;
    }

    private static void validateProductId(Long productId) {
        if (productId == null || productId <= 0) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_ID);
        }
    }

    private static void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new ProductException(ProductErrorCode.INVALID_STOCK_QUANTITY);
        }
    }

    private static void validatePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new ProductException(ProductErrorCode.INVALID_ORDER_QUANTITY);
        }
    }
}
