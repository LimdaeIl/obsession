package com.app.obsession.payment.infrastructure.persistence;

import com.app.obsession.payment.application.port.PaymentRepository;
import com.app.obsession.payment.domain.Payment;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class PaymentRepositoryImpl implements PaymentRepository {

    private final JpaPaymentRepository jpaPaymentRepository;

    @Override
    public Payment save(Payment payment) {
        return jpaPaymentRepository.save(payment);
    }

    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        return jpaPaymentRepository.findByOrderId(orderId);
    }

    @Override
    public Optional<Payment> findByTossOrderId(String tossOrderId) {
        return jpaPaymentRepository.findByTossOrderId(tossOrderId);
    }

    @Override
    public Optional<Payment> findByPaymentKey(String paymentKey) {
        return jpaPaymentRepository.findByPaymentKey(paymentKey);
    }
}
