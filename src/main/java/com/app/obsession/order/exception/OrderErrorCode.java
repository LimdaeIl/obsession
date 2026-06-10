package com.app.obsession.order.exception;

import com.app.obsession.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문: 주문을 찾을 수 없습니다."),

    INVALID_ORDER_ID(HttpStatus.BAD_REQUEST, "주문: 주문 ID가 올바르지 않습니다."),
    INVALID_MEMBER_ID(HttpStatus.BAD_REQUEST, "주문: 회원 ID가 올바르지 않습니다."),
    INVALID_ORDER_LINE(HttpStatus.BAD_REQUEST, "주문: 주문 상품 정보가 올바르지 않습니다."),
    EMPTY_ORDER_LINES(HttpStatus.BAD_REQUEST, "주문: 주문 상품은 1개 이상이어야 합니다."),

    INVALID_PRODUCT_ID(HttpStatus.BAD_REQUEST, "주문: 상품 ID가 올바르지 않습니다."),
    INVALID_PRODUCT_NAME(HttpStatus.BAD_REQUEST, "주문: 상품명은 필수입니다."),
    INVALID_PRODUCT_PRICE(HttpStatus.BAD_REQUEST, "주문: 상품 가격은 0보다 커야 합니다."),
    INVALID_ORDER_QUANTITY(HttpStatus.BAD_REQUEST, "주문: 주문 수량은 1 이상이어야 합니다."),

    ORDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "주문: 해당 주문에 접근할 권한이 없습니다."),
    ORDER_CANCEL_DENIED(HttpStatus.BAD_REQUEST, "주문: 취소할 수 없는 주문입니다."),
    ORDER_PAYMENT_DENIED(HttpStatus.BAD_REQUEST, "주문: 결제할 수 없는 주문입니다."),

    ONLY_CREATED_ORDER_CAN_BE_CANCELED(HttpStatus.BAD_REQUEST, "주문: 생성 상태의 주문만 취소할 수 있습니다."),
    ONLY_CREATED_ORDER_CAN_BE_PAID(HttpStatus.BAD_REQUEST, "주문: 생성 상태의 주문만 결제 완료 처리할 수 있습니다."),
    ONLY_CREATED_ORDER_CAN_BE_FAILED(HttpStatus.BAD_REQUEST, "주문: 생성 상태의 주문만 실패 처리할 수 있습니다.");

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
