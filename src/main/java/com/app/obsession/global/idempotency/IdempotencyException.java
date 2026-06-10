package com.app.obsession.global.idempotency;

import com.app.obsession.global.exception.AppException;

public class IdempotencyException extends AppException {

    public IdempotencyException(IdempotencyErrorCode errorCode) {
        super(errorCode);
    }
}
