package com.app.obsession.order.domain;

import com.app.obsession.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "v1_order_status_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class OrderStatusHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private OrderStatus toStatus;

    @Column(name = "reason", nullable = false, length = 200)
    private String reason;

    private OrderStatusHistory(
            Long orderId,
            OrderStatus fromStatus,
            OrderStatus toStatus,
            String reason
    ) {
        this.orderId = orderId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.reason = reason;
    }

    public static OrderStatusHistory record(
            Long orderId,
            OrderStatus fromStatus,
            OrderStatus toStatus,
            String reason
    ) {
        return new OrderStatusHistory(
                orderId,
                fromStatus,
                toStatus,
                reason
        );
    }
}
