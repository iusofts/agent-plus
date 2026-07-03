package com.iusofts.agentplus.basic.exception;

public class SystemBusinessException extends AbstractBusinessException {

    public SystemBusinessException(Throwable e) {
        super("SystemBusinessException", 500, e);
    }

    public SystemBusinessException(String message) {
        super(message, 500);
    }

    public SystemBusinessException(int code, String message) {
        super(message, code);
    }

}
