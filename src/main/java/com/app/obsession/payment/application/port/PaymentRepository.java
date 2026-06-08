package com.app.obsession.payment.application.port;

import com.app.obsession.payment.domain.Payment;
import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByTossOrderId(String tossOrderId);

    Optional<Payment> findByPaymentKey(String paymentKey);
}