package com.app.obsession.order.exception;

import com.app.obsession.global.exception.AppException;

public class OrderException extends AppException {

    public OrderException(OrderErrorCode errorCode) {
        super(errorCode);
    }

    public OrderException(OrderErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}
