package com.iusofts.agentplus.id.exception;

import com.iusofts.agentplus.basic.exception.AbstractBusinessException;

public class IdGenerationException extends AbstractBusinessException {

    private static final long serialVersionUID = 8492374252697203328L;

    public static final int ERROR_CODE = 710004;

    public IdGenerationException(String message) {
        super(message, ERROR_CODE);
    }

    public IdGenerationException(String message, Throwable throwable) {
        super(message, ERROR_CODE, throwable);
    }

    public IdGenerationException(Object data, String message) {
        super(data, message, ERROR_CODE);
    }

    public IdGenerationException(Object data, String message, Throwable throwable) {
        super(data, message, ERROR_CODE, throwable);
    }
}
