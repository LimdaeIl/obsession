package com.app.obsession.order.application;

import com.app.obsession.order.application.port.OrderRepository;
import com.app.obsession.order.domain.Order;
import com.app.obsession.order.domain.OrderLine;
import com.app.obsession.order.exception.OrderErrorCode;
import com.app.obsession.order.exception.OrderException;
import com.app.obsession.product.application.port.ProductStockRepository;
import com.app.obsession.product.domain.ProductStock;
import com.app.obsession.product.exception.ProductErrorCode;
import com.app.obsession.product.exception.ProductException;
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
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        if (!order.isOwnedBy(memberId)) {
            throw new OrderException(OrderErrorCode.ORDER_ACCESS_DENIED);
        }

        for (OrderLine orderLine : order.getOrderLines()) {
            ProductStock stock = productStockRepository.findByProductId(orderLine.getProductId())
                    .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_STOCK_NOT_FOUND));

            stock.release(orderLine.getQuantity());
        }

        order.cancel();
    }
}
