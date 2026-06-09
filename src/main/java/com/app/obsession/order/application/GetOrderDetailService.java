package com.app.obsession.order.application;

import com.app.obsession.order.application.port.OrderRepository;
import com.app.obsession.order.domain.Order;
import com.app.obsession.order.exception.OrderErrorCode;
import com.app.obsession.order.exception.OrderException;
import com.app.obsession.order.presentation.dto.OrderDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class GetOrderDetailService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public OrderDetailResponse getMyOrder(Long orderId, Long memberId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        if (!order.isOwnedBy(memberId)) {
            throw new OrderException(OrderErrorCode.ORDER_ACCESS_DENIED);
        }

        return OrderDetailResponse.from(order);
    }
}
