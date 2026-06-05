package com.app.obsession.global.exception;

import com.app.obsession.global.response.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j(topic = "GlobalExceptionHandler")
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String PROBLEM_BASE_URI = "about:blank/";

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleApp(
            AppException ex,
            HttpServletRequest request
    ) {
        ErrorCode code = ex.getErrorCode();
        String title = ((Enum<?>) code).name();

        return error(
                code.status(),
                title,
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBody(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<ErrorResponse.FieldError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> ErrorResponse.FieldError.of(e.getField(), e.getDefaultMessage()))
                .toList();

        return appError(AppErrorCode.INVALID_INPUT_VALUE, request, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<ErrorResponse.FieldError> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> {
                    String path = v.getPropertyPath().toString();
                    String field = path.contains(".")
                            ? path.substring(path.lastIndexOf('.') + 1)
                            : path;

                    return ErrorResponse.FieldError.of(field, v.getMessage());
                })
                .toList();

        return appError(AppErrorCode.INVALID_INPUT_VALUE, request, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return appError(AppErrorCode.INVALID_INPUT_VALUE, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        return appError(AppErrorCode.METHOD_NOT_ALLOWED, request);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request
    ) {
        return appError(AppErrorCode.ENTITY_NOT_FOUND, request);
    }

    @ExceptionHandler({
            AccessDeniedException.class,
            AuthorizationDeniedException.class
    })
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            Exception ex,
            HttpServletRequest request
    ) {
        return appError(AppErrorCode.ACCESS_DENIED, request, "권한이 없습니다.");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        return appError(AppErrorCode.UNAUTHORIZED, request, "인증이 필요합니다.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("처리되지 않은 예외", ex);
        return appError(AppErrorCode.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ErrorResponse> appError(
            AppErrorCode code,
            HttpServletRequest request
    ) {
        return appError(code, request, code.message(), null);
    }

    private ResponseEntity<ErrorResponse> appError(
            AppErrorCode code,
            HttpServletRequest request,
            String detail
    ) {
        return appError(code, request, detail, null);
    }

    private ResponseEntity<ErrorResponse> appError(
            AppErrorCode code,
            HttpServletRequest request,
            List<ErrorResponse.FieldError> errors
    ) {
        return appError(code, request, code.message(), errors);
    }

    private ResponseEntity<ErrorResponse> appError(
            AppErrorCode code,
            HttpServletRequest request,
            String detail,
            List<ErrorResponse.FieldError> errors
    ) {
        String title = code.name();

        return error(
                code.status(),
                title,
                detail,
                request,
                errors
        );
    }

    private ResponseEntity<ErrorResponse> error(
            HttpStatus status,
            String title,
            String detail,
            HttpServletRequest request,
            List<ErrorResponse.FieldError> errors
    ) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.problem(
                        problemType(title),
                        title,
                        status,
                        detail,
                        request.getRequestURI(),
                        title,
                        errors
                ));
    }

    private String problemType(String title) {
        return PROBLEM_BASE_URI + title.toLowerCase().replace('_', '-');
    }
}


