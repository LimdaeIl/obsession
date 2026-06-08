package com.app.obsession.order.domain;

import com.app.obsession.global.entity.BaseAuditEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "v1_order_lines")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class OrderLine extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "line_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineAmount;

    private OrderLine(
            Order order,
            Long productId,
            String productName,
            BigDecimal price,
            int quantity
    ) {
        if (order == null) {
            throw new IllegalArgumentException("주문은 필수입니다.");
        }
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("상품 ID가 올바르지 않습니다.");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("상품 가격은 0보다 커야 합니다.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("주문 수량은 1 이상이어야 합니다.");
        }

        this.order = order;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.lineAmount = price.multiply(BigDecimal.valueOf(quantity));
    }

    public static OrderLine create(
            Order order,
            Long productId,
            String productName,
            BigDecimal price,
            int quantity
    ) {
        return new OrderLine(order, productId, productName, price, quantity);
    }
}
