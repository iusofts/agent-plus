package com.iusofts.agentplus.basic.exception;

public class InvalidPermissionException extends AbstractBusinessException {

    public InvalidPermissionException() {
        super(ExceptionTypeEnum.INVALID_PERMISSION_EXCEPTION.getDetail(), ExceptionTypeEnum.INVALID_PERMISSION_EXCEPTION.getCode());
    }

    public InvalidPermissionException(String message) {
        super(message, ExceptionTypeEnum.INVALID_PERMISSION_EXCEPTION.getCode());
    }
}
