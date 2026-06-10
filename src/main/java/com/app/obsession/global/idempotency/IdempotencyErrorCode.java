package com.app.obsession.global.idempotency;

import com.app.obsession.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum IdempotencyErrorCode implements ErrorCode {

    INVALID_IDEMPOTENCY_KEY(HttpStatus.BAD_REQUEST, "멱등성: Idempotency-Key가 올바르지 않습니다."),
    IDEMPOTENCY_KEY_CONFLICT(HttpStatus.CONFLICT, "멱등성: 같은 키로 다른 요청을 보낼 수 없습니다."),
    IDEMPOTENCY_REQUEST_PROCESSING(HttpStatus.CONFLICT, "멱등성: 동일 요청이 처리 중입니다."),
    IDEMPOTENCY_REQUEST_FAILED(HttpStatus.CONFLICT, "멱등성: 이전 요청이 실패했습니다. 새 Idempotency-Key로 다시 요청해주세요.");;

    private final HttpStatus httpStatus;
    private final String messageTemplate;

    @Override
    public HttpStatus status() {
        return httpStatus;
    }

    @Override
    public String message() {
        return messageTemplate;
    }
}
