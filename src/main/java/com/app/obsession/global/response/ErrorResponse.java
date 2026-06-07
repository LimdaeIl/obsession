package com.app.obsession.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        String errorCode,
        List<Object> parameters,
        List<FieldError> errors
) {

    public static ErrorResponse problem(
            String type,
            String title,
            HttpStatus status,
            String detail,
            String instance,
            String errorCode,
            List<Object> parameters,
            List<FieldError> errors
    ) {
        return new ErrorResponse(
                type,
                title,
                status.value(),
                detail,
                instance,
                errorCode,
                emptyToNull(parameters),
                emptyToNull(errors)
        );
    }

    private static <T> List<T> emptyToNull(List<T> values) {
        return values == null || values.isEmpty() ? null : values;
    }

    public record FieldError(String field, String reason) {

        public static FieldError of(String field, String reason) {
            return new FieldError(field, reason);
        }
    }
}
