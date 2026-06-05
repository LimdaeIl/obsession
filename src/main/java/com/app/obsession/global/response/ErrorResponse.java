package com.app.obsession.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String type,        // problem type URI
        String title,       // 에러 이름 (ENUM name)
        int status,         // HTTP status code
        String detail,      // 사람이 읽을 메시지
        String instance,    // 요청 URI
        String errorCode,   // 내부 에러 코드 (ENUM name or A001 등)
        List<FieldError> errors
) {

    public static ErrorResponse problem(
            String type,
            String title,
            HttpStatus status,
            String detail,
            String instance,
            String errorCode,
            List<FieldError> errors
    ) {
        return new ErrorResponse(
                type,
                title,
                status.value(),
                detail,
                instance,
                errorCode,
                (errors == null || errors.isEmpty()) ? null : errors
        );
    }

    public record FieldError(String field, String reason) {
        public static FieldError of(String field, String reason) {
            return new FieldError(field, reason);
        }
    }
}
