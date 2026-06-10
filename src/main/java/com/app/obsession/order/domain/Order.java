package com.app.obsession.order.domain;

import com.app.obsession.global.entity.BaseAuditEntity;
import com.app.obsession.order.exception.OrderErrorCode;
import com.app.obsession.order.exception.OrderException;
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
@Table(name = "v1_orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Order extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> orderLines = new ArrayList<>();

    private Order(Long memberId) {
        if (memberId == null || memberId <= 0) {
            throw new OrderException(OrderErrorCode.INVALID_MEMBER_ID);
        }

        this.memberId = memberId;
        this.status = OrderStatus.CREATED;
        this.totalAmount = BigDecimal.ZERO;
    }

    public static Order create(Long memberId) {
        return new Order(memberId);
    }

    public void addOrderLine(Long productId, String productName, BigDecimal price, int quantity) {
        OrderLine orderLine = OrderLine.create(this, productId, productName, price, quantity);
        this.orderLines.add(orderLine);
        this.totalAmount = this.totalAmount.add(orderLine.getLineAmount());
    }

    public boolean isOwnedBy(Long memberId) {
        return memberId != null && this.memberId.equals(memberId);
    }

    public void cancel() {
        if (this.status != OrderStatus.CREATED && this.status != OrderStatus.PAID) {
            throw new OrderException(OrderErrorCode.ONLY_CANCELABLE_ORDER_CAN_BE_CANCELED);
        }

        this.status = OrderStatus.CANCELED;
    }

    public void markPaid() {
        if (this.status != OrderStatus.CREATED) {
            throw new OrderException(OrderErrorCode.ONLY_CREATED_ORDER_CAN_BE_PAID);
        }

        this.status = OrderStatus.PAID;
    }

    public void markFailed() {
        if (this.status != OrderStatus.CREATED) {
            throw new OrderException(OrderErrorCode.ONLY_CREATED_ORDER_CAN_BE_FAILED);
        }

        this.status = OrderStatus.FAILED;
    }

    public boolean isPayableBy(Long memberId) {
        return isOwnedBy(memberId) && this.status == OrderStatus.CREATED;
    }

    public void cancelCreatedOrder() {
        if (this.status != OrderStatus.CREATED) {
            throw new OrderException(OrderErrorCode.ONLY_CANCELABLE_ORDER_CAN_BE_CANCELED);
        }

        this.status = OrderStatus.CANCELED;
    }

    public void requestPaidOrderCancel() {
        if (this.status != OrderStatus.PAID) {
            throw new OrderException(OrderErrorCode.ONLY_CANCELABLE_ORDER_CAN_BE_CANCELED);
        }

        this.status = OrderStatus.CANCEL_REQUESTED;
    }

    public void completePaidOrderCancel() {
        if (this.status != OrderStatus.CANCEL_REQUESTED) {
            throw new OrderException(OrderErrorCode.ONLY_CANCELABLE_ORDER_CAN_BE_CANCELED);
        }

        this.status = OrderStatus.CANCELED;
    }

    public void validateCreatable() {
        if (orderLines.isEmpty()) {
            throw new OrderException(OrderErrorCode.EMPTY_ORDER_LINES);
        }
    }

}
