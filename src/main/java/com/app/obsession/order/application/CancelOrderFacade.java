package com.app.obsession.order.application;

import com.app.obsession.order.application.port.OrderRepository;
import com.app.obsession.order.domain.Order;
import com.app.obsession.order.domain.OrderStatus;
import com.app.obsession.order.exception.OrderErrorCode;
import com.app.obsession.order.exception.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CancelOrderFacade {

    private final OrderRepository orderRepository;
    private final CancelOrderService cancelOrderService;
    private final CancelPaidOrderService cancelPaidOrderService;

    @Transactional
    public void cancel(
            String idempotencyKey,
            Long orderId,
            Long memberId,
            String cancelReason
    ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        if (!order.isOwnedBy(memberId)) {
            throw new OrderException(OrderErrorCode.ORDER_ACCESS_DENIED);
        }

        if (order.getStatus() == OrderStatus.CREATED) {
            cancelOrderService.cancel(order);
            return;
        }

        if (order.getStatus() == OrderStatus.PAID) {
            cancelPaidOrderService.cancel(
                    idempotencyKey,
                    orderId,
                    memberId,
                    cancelReason
            );
            return;
        }

        throw new OrderException(OrderErrorCode.ORDER_CANCEL_DENIED);
    }
}
