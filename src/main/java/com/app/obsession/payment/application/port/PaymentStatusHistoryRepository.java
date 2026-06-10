package com.app.obsession.payment.application.port;

import com.app.obsession.payment.domain.PaymentStatusHistory;

public interface PaymentStatusHistoryRepository {

    PaymentStatusHistory save(PaymentStatusHistory history);
}
