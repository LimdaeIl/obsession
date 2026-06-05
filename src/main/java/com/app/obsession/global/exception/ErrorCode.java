package com.app.obsession.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    HttpStatus status();

    String message();

    default String code() {
        return this instanceof Enum<?> enumCode
                ? enumCode.name()
                : this.getClass().getSimpleName();
    }

    default String format(Object... args) {
        return args == null || args.length == 0
                ? message()
                : String.format(message(), args);
    }
}
