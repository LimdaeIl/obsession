package com.app.obsession.order.application;

import com.app.obsession.order.application.port.OrderRepository;
import com.app.obsession.order.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class GetOrderDetailService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public Order getMyOrder(Long orderId, Long memberId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (order.isOwnedBy(memberId)) {
            throw new IllegalStateException("주문을 조회할 권한이 없습니다.");
        }

        return order;
    }
}
