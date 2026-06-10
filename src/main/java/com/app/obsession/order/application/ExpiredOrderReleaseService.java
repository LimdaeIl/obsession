package com.app.obsession.order.application;

import com.app.obsession.order.application.port.OrderRepository;
import com.app.obsession.order.application.port.OrderStatusHistoryRepository;
import com.app.obsession.order.domain.Order;
import com.app.obsession.order.domain.OrderLine;
import com.app.obsession.order.domain.OrderStatus;
import com.app.obsession.order.domain.OrderStatusHistory;
import com.app.obsession.payment.application.port.PaymentRepository;
import com.app.obsession.payment.application.port.PaymentStatusHistoryRepository;
import com.app.obsession.payment.domain.Payment;
import com.app.obsession.payment.domain.PaymentStatus;
import com.app.obsession.payment.domain.PaymentStatusHistory;
import com.app.obsession.product.application.port.ProductStockRepository;
import com.app.obsession.product.domain.ProductStock;
import com.app.obsession.product.exception.ProductErrorCode;
import com.app.obsession.product.exception.ProductException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ExpiredOrderReleaseService {

    private static final long ORDER_PAYMENT_TIMEOUT_MINUTES = 30;

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ProductStockRepository productStockRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final PaymentStatusHistoryRepository paymentStatusHistoryRepository;
    private final Clock clock;

    @Transactional
    public void releaseExpiredOrders() {
        LocalDateTime expiredBefore = LocalDateTime.now(clock)
                .minusMinutes(ORDER_PAYMENT_TIMEOUT_MINUTES);

        List<Order> orders = orderRepository.findExpiredCreatedOrders(expiredBefore);

        for (Order order : orders) {
            expire(order);
        }
    }

    private void expire(Order order) {
        releaseStocks(order);

        OrderStatus orderFromStatus = order.getStatus();
        order.markFailed();

        orderStatusHistoryRepository.save(
                OrderStatusHistory.record(
                        order.getId(),
                        orderFromStatus,
                        OrderStatus.FAILED,
                        "ORDER_PAYMENT_TIMEOUT"
                )
        );

        paymentRepository.findByOrderId(order.getId())
                .ifPresent(this::failPayment);
    }

    private void failPayment(Payment payment) {
        if (!payment.isReady()) {
            return;
        }

        PaymentStatus paymentFromStatus = payment.getStatus();
        payment.fail();

        paymentStatusHistoryRepository.save(
                PaymentStatusHistory.record(
                        payment.getId(),
                        payment.getOrderId(),
                        paymentFromStatus,
                        PaymentStatus.FAILED,
                        "ORDER_PAYMENT_TIMEOUT"
                )
        );
    }

    private void releaseStocks(Order order) {
        for (OrderLine orderLine : order.getOrderLines()) {
            ProductStock stock = productStockRepository.findByProductId(orderLine.getProductId())
                    .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_STOCK_NOT_FOUND));

            stock.release(orderLine.getQuantity());
        }
    }
}
