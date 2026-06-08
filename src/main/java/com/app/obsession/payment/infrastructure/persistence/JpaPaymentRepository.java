package com.app.obsession.payment.infrastructure.persistence;

import com.app.obsession.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPaymentRepository extends JpaRepository<Payment, Long> {
}
