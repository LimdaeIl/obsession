package com.app.obsession.payment.exception;

import com.app.obsession.global.exception.AppException;

public class PaymentException extends AppException {

    public PaymentException(PaymentErrorCode errorCode) {
        super(errorCode);
    }

    public PaymentException(PaymentErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}
