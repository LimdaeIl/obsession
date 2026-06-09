package com.app.obsession.order.domain;

import com.app.obsession.global.entity.BaseAuditEntity;
import com.app.obsession.order.exception.OrderErrorCode;
import com.app.obsession.order.exception.OrderException;
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
            throw new OrderException(OrderErrorCode.INVALID_ORDER_LINE);
        }
        if (productId == null || productId <= 0) {
            throw new OrderException(OrderErrorCode.INVALID_PRODUCT_ID);
        }
        if (productName == null || productName.isBlank()) {
            throw new OrderException(OrderErrorCode.INVALID_PRODUCT_NAME);
        }
        if (price == null || price.signum() <= 0) {
            throw new OrderException(OrderErrorCode.INVALID_PRODUCT_PRICE);
        }
        if (quantity <= 0) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_QUANTITY);
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
