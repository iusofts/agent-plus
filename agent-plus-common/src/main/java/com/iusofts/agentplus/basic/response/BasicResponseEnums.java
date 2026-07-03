package com.iusofts.agentplus.basic.response;

public enum BasicResponseEnums {

    SUCCESS(0, "SUCCESS"),
    FAIL(500, "FAIL");


    private int code;
    private String message;


    BasicResponseEnums(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
