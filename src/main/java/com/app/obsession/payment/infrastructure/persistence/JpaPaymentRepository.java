package com.app.obsession.payment.infrastructure.persistence;

import com.app.obsession.payment.domain.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByTossOrderId(String tossOrderId);

    Optional<Payment> findByPaymentKey(String paymentKey);
}
