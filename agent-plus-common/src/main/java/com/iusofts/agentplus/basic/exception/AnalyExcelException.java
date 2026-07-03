package com.iusofts.agentplus.basic.exception;

public class AnalyExcelException extends AbstractBusinessException {

    public AnalyExcelException(Throwable e) {
        super("AnalyExcelException", 500, e);
    }

    public AnalyExcelException(String message) {
        super(message, 500);
    }

    public AnalyExcelException(int code, String message) {
        super(message, code);
    }


}
