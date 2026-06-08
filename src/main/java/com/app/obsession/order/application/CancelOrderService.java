package com.app.obsession.order.application;

import com.app.obsession.order.application.port.OrderRepository;
import com.app.obsession.order.domain.Order;
import com.app.obsession.order.domain.OrderLine;
import com.app.obsession.product.application.port.ProductStockRepository;
import com.app.obsession.product.domain.ProductStock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CancelOrderService {

    private final OrderRepository orderRepository;
    private final ProductStockRepository productStockRepository;

    @Transactional
    public void cancel(Long orderId, Long memberId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (order.isOwnedBy(memberId)) {
            throw new IllegalStateException("주문을 취소할 권한이 없습니다.");
        }

        for (OrderLine orderLine : order.getOrderLines()) {
            ProductStock stock = productStockRepository.findByProductId(orderLine.getProductId())
                    .orElseThrow(() -> new IllegalStateException("상품 재고 정보를 찾을 수 없습니다."));

            stock.release(orderLine.getQuantity());
        }

        order.cancel();
    }
}
