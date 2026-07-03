package com.iusofts.basic.exception;

/**
 * 参数异常类
 *
 * @author wyh
 */
public class InvalidParamException extends AbstractBusinessException {
    private static final long serialVersionUID = 1L;

    private Object data;

    public InvalidParamException() {
        super(ExceptionTypeEnum.ILLEGAL_ARGUMENT_EXCEPTION.getDetail(), ExceptionTypeEnum.ILLEGAL_ARGUMENT_EXCEPTION.getCode());
    }

    public InvalidParamException(String msg) {
        super(msg, ExceptionTypeEnum.ILLEGAL_ARGUMENT_EXCEPTION.getCode());
    }

    public InvalidParamException(String msg, Object data) {
        super(msg, ExceptionTypeEnum.ILLEGAL_ARGUMENT_EXCEPTION.getCode());
        this.data = data;
    }

    public InvalidParamException(String msg, Throwable t) {
        super(msg, ExceptionTypeEnum.ILLEGAL_ARGUMENT_EXCEPTION.getCode(), t);
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
