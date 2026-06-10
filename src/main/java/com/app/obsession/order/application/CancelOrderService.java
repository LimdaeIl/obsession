package com.app.obsession.order.application;

import com.app.obsession.order.application.port.OrderRepository;
import com.app.obsession.order.application.port.OrderStatusHistoryRepository;
import com.app.obsession.order.domain.Order;
import com.app.obsession.order.domain.OrderLine;
import com.app.obsession.order.domain.OrderStatus;
import com.app.obsession.order.domain.OrderStatusHistory;
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

    private final ProductStockRepository productStockRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public void cancel(Order order) {
        for (OrderLine orderLine : order.getOrderLines()) {
            ProductStock stock = productStockRepository.findByProductId(orderLine.getProductId())
                    .orElseThrow(
                            () -> new ProductException(ProductErrorCode.PRODUCT_STOCK_NOT_FOUND));

            stock.release(orderLine.getQuantity());
        }

        OrderStatus fromStatus = order.getStatus();
        order.cancel();

        orderStatusHistoryRepository.save(
                OrderStatusHistory.record(
                        order.getId(),
                        fromStatus,
                        OrderStatus.CANCELED,
                        "ORDER_CANCELED"
                )
        );
    }
}

