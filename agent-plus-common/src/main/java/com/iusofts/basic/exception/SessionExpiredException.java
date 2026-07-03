package com.iusofts.basic.exception;

public class SessionExpiredException extends AbstractBusinessException {

    public SessionExpiredException() {
        super(ExceptionTypeEnum.LOGIN_INVALID_EXCEPTION.getDetail(), ExceptionTypeEnum.LOGIN_INVALID_EXCEPTION.getCode());
    }
}