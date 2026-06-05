package com.app.obsession.global.exception;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final transient Object[] args;

    public AppException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
        this.args = new Object[0];
    }

    public AppException(ErrorCode errorCode, Object... args) {
        super(errorCode.format(args));
        this.errorCode = errorCode;
        this.args = args == null ? new Object[0] : args;
    }

    public List<Object> getParameters() {
        return Arrays.asList(args);
    }
}
