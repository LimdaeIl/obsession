package com.app.obsession.payment.infrastructure.persistence;

import com.app.obsession.payment.domain.PaymentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPaymentStatusHistoryRepository extends JpaRepository<PaymentStatusHistory, Long> {
}
