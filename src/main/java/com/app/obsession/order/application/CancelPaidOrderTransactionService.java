package com.app.obsession.order.application;

import com.app.obsession.order.application.port.OrderRepository;
import com.app.obsession.order.application.port.OrderStatusHistoryRepository;
import com.app.obsession.order.domain.Order;
import com.app.obsession.order.domain.OrderLine;
import com.app.obsession.order.domain.OrderStatus;
import com.app.obsession.order.domain.OrderStatusHistory;
import com.app.obsession.order.exception.OrderErrorCode;
import com.app.obsession.order.exception.OrderException;
import com.app.obsession.payment.application.command.CancelPaymentCommand;
import com.app.obsession.payment.application.port.PaymentRepository;
import com.app.obsession.payment.application.port.PaymentStatusHistoryRepository;
import com.app.obsession.payment.domain.Payment;
import com.app.obsession.payment.domain.PaymentStatus;
import com.app.obsession.payment.domain.PaymentStatusHistory;
import com.app.obsession.payment.exception.PaymentErrorCode;
import com.app.obsession.payment.exception.PaymentException;
import com.app.obsession.product.application.port.ProductStockRepository;
import com.app.obsession.product.domain.ProductStock;
import com.app.obsession.product.exception.ProductErrorCode;
import com.app.obsession.product.exception.ProductException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CancelPaidOrderTransactionService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ProductStockRepository productStockRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final PaymentStatusHistoryRepository paymentStatusHistoryRepository;

    @Transactional
    public CancelPaymentCommand requestCancel(Long orderId, Long memberId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        if (!order.isOwnedBy(memberId)) {
            throw new OrderException(OrderErrorCode.ORDER_ACCESS_DENIED);
        }

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        OrderStatus orderFromStatus = order.getStatus();
        PaymentStatus paymentFromStatus = payment.getStatus();

        order.requestPaidOrderCancel();
        payment.requestCancel();

        orderStatusHistoryRepository.save(OrderStatusHistory.record(order.getId(), orderFromStatus,
                OrderStatus.CANCEL_REQUESTED, "PAYMENT_CANCEL_REQUESTED"));

        paymentStatusHistoryRepository.save(
                PaymentStatusHistory.record(payment.getId(), payment.getOrderId(),
                        paymentFromStatus, PaymentStatus.CANCEL_REQUESTED,
                        "PAYMENT_CANCEL_REQUESTED"));

        return new CancelPaymentCommand(order.getId(), payment.getPaymentKey());
    }

    @Transactional
    public void completeCancel(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        restoreSoldStocks(order);

        OrderStatus orderFromStatus = order.getStatus();
        PaymentStatus paymentFromStatus = payment.getStatus();

        order.completePaidOrderCancel();
        payment.cancel();

        orderStatusHistoryRepository.save(
                OrderStatusHistory.record(order.getId(), orderFromStatus, OrderStatus.CANCELED,
                        "PAYMENT_CANCELED"));

        paymentStatusHistoryRepository.save(
                PaymentStatusHistory.record(payment.getId(), payment.getOrderId(),
                        paymentFromStatus, PaymentStatus.CANCELED, "PAYMENT_CANCELED"));
    }

    private void restoreSoldStocks(Order order) {
        for (OrderLine orderLine : order.getOrderLines()) {
            ProductStock stock = productStockRepository.findByProductId(orderLine.getProductId())
                    .orElseThrow(
                            () -> new ProductException(ProductErrorCode.PRODUCT_STOCK_NOT_FOUND));

            stock.restoreSold(orderLine.getQuantity());
        }
    }
}
