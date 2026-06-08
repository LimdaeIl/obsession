package com.app.obsession.order.presentation.dto;

import com.app.obsession.order.domain.Order;
import com.app.obsession.order.domain.OrderLine;
import com.app.obsession.order.domain.OrderStatus;
import java.math.BigDecimal;
import java.util.List;

public record OrderDetailResponse(
        Long orderId,
        OrderStatus status,
        BigDecimal totalAmount,
        List<OrderLineResponse> orderLines
) {

    public static OrderDetailResponse from(Order order) {
        return new OrderDetailResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getOrderLines()
                        .stream()
                        .map(OrderLineResponse::from)
                        .toList()
        );
    }

    public record OrderLineResponse(
            Long productId,
            String productName,
            BigDecimal price,
            int quantity,
            BigDecimal lineAmount
    ) {

        public static OrderLineResponse from(OrderLine orderLine) {
            return new OrderLineResponse(
                    orderLine.getProductId(),
                    orderLine.getProductName(),
                    orderLine.getPrice(),
                    orderLine.getQuantity(),
                    orderLine.getLineAmount()
            );
        }
    }
}
