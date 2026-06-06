package com.app.obsession.global.security.exception;

import com.app.obsession.global.exception.AppException;

public class AuthException extends AppException {

    public AuthException(AuthErrorCode errorCode) {
        super(errorCode);
    }

    public AuthException(AuthErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}

