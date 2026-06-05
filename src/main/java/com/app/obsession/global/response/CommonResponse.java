package com.app.obsession.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommonResponse<T>(
        boolean success,
        int status,
        String message,
        T data,
        LocalDateTime timestamp
) {

    public static <T> CommonResponse<T> ok(T data) {
        return new CommonResponse<>(
                true,
                200,
                "요청이 성공적으로 처리되었습니다.",
                data,
                LocalDateTime.now()
        );
    }

    public static <T> CommonResponse<T> ok(String message, T data) {
        return new CommonResponse<>(
                true,
                200,
                message,
                data,
                LocalDateTime.now()
        );
    }

    public static CommonResponse<Void> ok() {
        return new CommonResponse<>(
                true,
                200,
                "요청이 성공적으로 처리되었습니다.",
                null,
                LocalDateTime.now()
        );
    }

    public static <T> CommonResponse<T> created(T data) {
        return new CommonResponse<>(
                true,
                201,
                "리소스가 생성되었습니다.",
                data,
                LocalDateTime.now()
        );
    }

    public static <T> CommonResponse<T> created(String message, T data) {
        return new CommonResponse<>(
                true,
                201,
                message,
                data,
                LocalDateTime.now()
        );
    }

    public static CommonResponse<Void> noContent() {
        return new CommonResponse<>(
                true,
                204,
                "요청이 성공적으로 처리되었습니다.",
                null,
                LocalDateTime.now()
        );
    }
}