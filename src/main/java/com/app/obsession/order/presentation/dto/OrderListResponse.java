package com.app.obsession.order.presentation.dto;

import com.app.obsession.order.domain.Order;
import com.app.obsession.order.domain.OrderStatus;
import java.math.BigDecimal;

public record OrderListResponse(
        Long orderId,
        OrderStatus status,
        BigDecimal totalAmount
) {

    public static OrderListResponse from(Order order) {
        return new OrderListResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount()
        );
    }
}
