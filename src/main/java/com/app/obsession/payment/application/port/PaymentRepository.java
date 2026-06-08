package com.app.obsession.payment.application.port;

import com.app.obsession.payment.domain.Payment;

public interface PaymentRepository {

    Payment save(Payment payment);
}
