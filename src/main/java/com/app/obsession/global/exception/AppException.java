package com.app.obsession.global.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final transient Object[] args;

    public AppException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
        this.args = null;
    }

    public AppException(ErrorCode errorCode, Object... args) {
        super(errorCode.message());
        this.errorCode = errorCode;
        this.args = args;
    }

    public AppException(String message, ErrorCode errorCode, Object[] args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }
}

