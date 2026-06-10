package com.app.obsession.order.application;

import com.app.obsession.order.application.port.OrderRepository;
import com.app.obsession.order.application.port.OrderStatusHistoryRepository;
import com.app.obsession.order.domain.Order;
import com.app.obsession.order.domain.OrderLine;
import com.app.obsession.order.domain.OrderStatus;
import com.app.obsession.order.domain.OrderStatusHistory;
import com.app.obsession.order.exception.OrderErrorCode;
import com.app.obsession.order.exception.OrderException;
import com.app.obsession.payment.application.port.PaymentRepository;
import com.app.obsession.payment.application.port.PaymentStatusHistoryRepository;
import com.app.obsession.payment.domain.Payment;
import com.app.obsession.payment.domain.PaymentStatus;
import com.app.obsession.payment.domain.PaymentStatusHistory;
import com.app.obsession.payment.exception.PaymentErrorCode;
import com.app.obsession.payment.exception.PaymentException;
import com.app.obsession.payment.infrastructure.external.TossPaymentCancelResponse;
import com.app.obsession.payment.infrastructure.external.TossPaymentClient;
import com.app.obsession.product.application.port.ProductStockRepository;
import com.app.obsession.product.domain.ProductStock;
import com.app.obsession.product.exception.ProductErrorCode;
import com.app.obsession.product.exception.ProductException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CancelPaidOrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ProductStockRepository productStockRepository;
    private final TossPaymentClient tossPaymentClient;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final PaymentStatusHistoryRepository paymentStatusHistoryRepository;

    @Transactional
    public void cancel(
            Long orderId,
            Long memberId,
            String cancelReason
    ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        if (!order.isOwnedBy(memberId)) {
            throw new OrderException(OrderErrorCode.ORDER_ACCESS_DENIED);
        }

        if (order.getStatus() != OrderStatus.PAID) {
            throw new OrderException(OrderErrorCode.ORDER_CANCEL_DENIED);
        }

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.isApproved()) {
            throw new PaymentException(PaymentErrorCode.ONLY_APPROVED_PAYMENT_CAN_BE_CANCELED);
        }

        TossPaymentCancelResponse response = tossPaymentClient.cancel(
                payment.getPaymentKey(),
                cancelReason
        );

        if (!"CANCELED".equals(response.status())) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_CANCEL_FAILED);
        }

        restoreSoldStocks(order);

        OrderStatus orderFromStatus = order.getStatus();
        order.cancel();

        orderStatusHistoryRepository.save(
                OrderStatusHistory.record(
                        order.getId(),
                        orderFromStatus,
                        OrderStatus.CANCELED,
                        "PAYMENT_CANCELED"
                )
        );

        PaymentStatus paymentFromStatus = payment.getStatus();
        payment.cancel();

        paymentStatusHistoryRepository.save(
                PaymentStatusHistory.record(
                        payment.getId(),
                        payment.getOrderId(),
                        paymentFromStatus,
                        PaymentStatus.CANCELED,
                        "PAYMENT_CANCELED"
                )
        );
    }

    private void restoreSoldStocks(Order order) {
        for (OrderLine orderLine : order.getOrderLines()) {
            ProductStock stock = productStockRepository.findByProductId(orderLine.getProductId())
                    .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_STOCK_NOT_FOUND));

            stock.restoreSold(orderLine.getQuantity());
        }
    }
}
