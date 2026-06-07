package com.app.obsession.product.domain;

import com.app.obsession.global.entity.BaseAuditEntity;
import jakarta.persistence.*;
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
            throw new IllegalStateException("판매 가능 재고보다 많이 차감할 수 없습니다.");
        }

        this.totalQuantity -= quantity;
    }

    public void reserve(int quantity) {
        validatePositiveQuantity(quantity);

        if (!hasAvailableQuantity(quantity)) {
            throw new IllegalStateException("재고가 부족합니다.");
        }

        this.reservedQuantity += quantity;
    }

    public void release(int quantity) {
        validatePositiveQuantity(quantity);

        if (this.reservedQuantity < quantity) {
            throw new IllegalStateException("예약 재고보다 많이 해제할 수 없습니다.");
        }

        this.reservedQuantity -= quantity;
    }

    public void confirm(int quantity) {
        validatePositiveQuantity(quantity);

        if (this.reservedQuantity < quantity) {
            throw new IllegalStateException("예약 재고보다 많이 판매 확정할 수 없습니다.");
        }

        this.reservedQuantity -= quantity;
        this.soldQuantity += quantity;
    }

    private static void validateProductId(Long productId) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("상품 ID가 올바르지 않습니다.");
        }
    }

    private static void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("재고 수량은 0 이상이어야 합니다.");
        }
    }

    private static void validatePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
    }
}
