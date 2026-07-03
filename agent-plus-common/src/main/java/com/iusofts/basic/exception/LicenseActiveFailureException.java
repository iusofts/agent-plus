package com.iusofts.basic.exception;

public class LicenseActiveFailureException extends AbstractBusinessException {

    public LicenseActiveFailureException() {
        super(ExceptionTypeEnum.LICENSE_ACTIVE_FAILURE_EXCEPTION.getDetail(), ExceptionTypeEnum.LICENSE_ACTIVE_FAILURE_EXCEPTION.getCode());
    }
}