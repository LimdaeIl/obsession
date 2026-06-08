package com.app.obsession.product.exception;

import com.app.obsession.global.exception.AppException;

public class ProductException extends AppException {

    public ProductException(ProductErrorCode errorCode) {
        super(errorCode);
    }

    public ProductException(ProductErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

}
