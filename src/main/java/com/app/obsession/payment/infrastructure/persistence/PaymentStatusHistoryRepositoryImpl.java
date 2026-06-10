package com.app.obsession.payment.infrastructure.persistence;

import com.app.obsession.payment.application.port.PaymentStatusHistoryRepository;
import com.app.obsession.payment.domain.PaymentStatusHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class PaymentStatusHistoryRepositoryImpl implements PaymentStatusHistoryRepository {

    private final JpaPaymentStatusHistoryRepository jpaPaymentStatusHistoryRepository;

    @Override
    public PaymentStatusHistory save(PaymentStatusHistory history) {
        return jpaPaymentStatusHistoryRepository.save(history);
    }
}
