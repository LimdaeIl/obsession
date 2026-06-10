package com.app.obsession.payment.exception;

import com.app.obsession.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제: 결제 정보를 찾을 수 없습니다."),

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "결제: 주문을 찾을 수 없습니다."),
    INVALID_ORDER_NUMBER(HttpStatus.BAD_REQUEST, "결제: 주문번호 형식이 올바르지 않습니다."),
    NOT_PAYABLE_ORDER(HttpStatus.BAD_REQUEST, "결제: 결제할 수 없는 주문입니다."),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "결제: 결제 금액이 주문 금액과 일치하지 않습니다."),

    PAYMENT_CONFIRM_FAILED(HttpStatus.BAD_GATEWAY, "결제: 결제 승인 요청에 실패했습니다."),
    PAYMENT_NOT_APPROVED(HttpStatus.BAD_GATEWAY, "결제: 결제가 정상 승인되지 않았습니다."),
    DUPLICATE_PAYMENT(HttpStatus.CONFLICT, "결제: 이미 승인된 결제입니다."),

    PRODUCT_STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "결제: 상품 재고 정보를 찾을 수 없습니다."),
    INVALID_ORDER_ID(HttpStatus.BAD_REQUEST, "결제: 주문 ID가 올바르지 않습니다."),
    INVALID_PAYMENT_KEY(HttpStatus.BAD_REQUEST, "결제: 결제 키가 올바르지 않습니다."),

    ONLY_READY_PAYMENT_CAN_BE_APPROVED(HttpStatus.BAD_REQUEST, "결제: 준비 상태의 결제만 승인 처리할 수 있습니다."),
    ONLY_READY_PAYMENT_CAN_BE_FAILED(HttpStatus.BAD_REQUEST, "결제: 준비 상태의 결제만 실패 처리할 수 있습니다."),
    ONLY_APPROVED_PAYMENT_CAN_BE_CANCELED(HttpStatus.BAD_REQUEST, "결제: 승인 완료된 결제만 취소 처리할 수 있습니다."),
    PAYMENT_CANCEL_FAILED(HttpStatus.BAD_GATEWAY, "결제: 결제 취소 요청에 실패했습니다.");

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
