package com.app.obsession.order.domain;

import com.app.obsession.global.entity.BaseAuditEntity;
import jakarta.persistence.*;
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
            throw new IllegalArgumentException("회원 ID가 올바르지 않습니다.");
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
        return !this.memberId.equals(memberId);
    }

    public void cancel() {
        if (this.status != OrderStatus.CREATED) {
            throw new IllegalStateException("생성 상태의 주문만 취소할 수 있습니다.");
        }

        this.status = OrderStatus.CANCELED;
    }
}
