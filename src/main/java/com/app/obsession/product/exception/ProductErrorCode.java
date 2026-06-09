package com.app.obsession.product.exception;

import com.app.obsession.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품: 상품을 찾을 수 없습니다."),

    INVALID_PRODUCT_ID(HttpStatus.BAD_REQUEST, "상품: 상품 ID가 올바르지 않습니다."),
    INVALID_SELLER_ID(HttpStatus.BAD_REQUEST, "상품: 판매자 ID가 올바르지 않습니다."),
    INVALID_PRODUCT_NAME(HttpStatus.BAD_REQUEST, "상품: 상품명은 필수입니다."),
    PRODUCT_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "상품: 상품명은 100자를 초과할 수 없습니다."),
    INVALID_PRODUCT_DESCRIPTION(HttpStatus.BAD_REQUEST, "상품: 상품 설명은 필수입니다."),
    PRODUCT_DESCRIPTION_TOO_LONG(HttpStatus.BAD_REQUEST, "상품: 상품 설명은 1000자를 초과할 수 없습니다."),
    INVALID_PRODUCT_PRICE(HttpStatus.BAD_REQUEST, "상품: 상품 가격은 0보다 커야 합니다."),

    INVALID_PRODUCT_STATUS(HttpStatus.BAD_REQUEST, "상품: 상품 상태는 필수입니다."),
    INVALID_INITIAL_PRODUCT_STATUS(HttpStatus.BAD_REQUEST, "상품: 상품 등록 시 사용할 수 없는 상태입니다."),
    DELETED_PRODUCT_CANNOT_BE_UPDATED(HttpStatus.BAD_REQUEST, "상품: 삭제된 상품은 수정할 수 없습니다."),
    DELETED_PRODUCT_CANNOT_BE_SOLD(HttpStatus.BAD_REQUEST, "상품: 삭제된 상품은 판매할 수 없습니다."),
    DELETED_PRODUCT_CANNOT_STOP_SALE(HttpStatus.BAD_REQUEST, "상품: 삭제된 상품은 판매 중지할 수 없습니다."),
    ONLY_ON_SALE_PRODUCT_CAN_BE_SOLD_OUT(HttpStatus.BAD_REQUEST, "상품: 판매 중인 상품만 품절 처리할 수 있습니다."),
    ALREADY_DELETED_PRODUCT(HttpStatus.BAD_REQUEST, "상품: 이미 삭제된 상품입니다."),
    ONLY_SELLABLE_PRODUCT_CAN_BE_ORDERED(HttpStatus.BAD_REQUEST, "상품: 판매 중인 상품만 주문할 수 있습니다."),

    INVALID_IMAGE_URL(HttpStatus.BAD_REQUEST, "상품: 상품 이미지 URL은 필수입니다."),
    PRODUCT_IMAGE_URL_TOO_LONG(HttpStatus.BAD_REQUEST, "상품: 상품 이미지 URL은 500자를 초과할 수 없습니다."),
    INVALID_IMAGE_SORT_ORDER(HttpStatus.BAD_REQUEST, "상품: 이미지 정렬 순서는 0 이상이어야 합니다."),
    INVALID_IMAGE_ID(HttpStatus.BAD_REQUEST, "상품: 이미지 ID는 필수입니다."),

    PRODUCT_STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "상품: 상품 재고 정보를 찾을 수 없습니다."),
    INVALID_STOCK_QUANTITY(HttpStatus.BAD_REQUEST, "상품: 재고 수량은 0 이상이어야 합니다."),
    INVALID_ORDER_QUANTITY(HttpStatus.BAD_REQUEST, "상품: 수량은 1 이상이어야 합니다."),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "상품: 재고가 부족합니다."),
    CANNOT_DECREASE_MORE_THAN_AVAILABLE_STOCK(HttpStatus.BAD_REQUEST, "상품: 판매 가능 재고보다 많이 차감할 수 없습니다."),
    CANNOT_RELEASE_MORE_THAN_RESERVED_STOCK(HttpStatus.BAD_REQUEST, "상품: 예약 재고보다 많이 해제할 수 없습니다."),
    CANNOT_CONFIRM_MORE_THAN_RESERVED_STOCK(HttpStatus.BAD_REQUEST, "상품: 예약 재고보다 많이 판매 확정할 수 없습니다."),
    ON_SALE_PRODUCT_REQUIRES_STOCK(HttpStatus.BAD_REQUEST, "상품: 판매중 상품은 재고가 1개 이상이어야 합니다."),

    PRODUCT_CREATE_FORBIDDEN(HttpStatus.FORBIDDEN, "상품: 상품을 등록할 권한이 없습니다."),
    PRODUCT_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "상품: 상품을 수정할 권한이 없습니다."),
    PRODUCT_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "상품: 상품을 삭제할 권한이 없습니다."),
    NOT_ON_SALE_PRODUCT(HttpStatus.BAD_REQUEST, "상품: 판매 중인 상품이 아닙니다."),;

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
