package com.app.obsession.member.exception;

import com.app.obsession.global.exception.AppException;

public class MemberException extends AppException {

    public MemberException(MemberErrorCode errorCode) {
        super(errorCode);
    }

    public MemberException(MemberErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}
