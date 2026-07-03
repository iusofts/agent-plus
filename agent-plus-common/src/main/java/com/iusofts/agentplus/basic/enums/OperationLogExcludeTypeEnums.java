package com.iusofts.agentplus.basic.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum OperationLogExcludeTypeEnums {

    URI(1, "地址"),
    REQ(2, "入参"),
    RES(3, "返回值");

    private static Map<Integer, OperationLogExcludeTypeEnums> map = Arrays.stream(OperationLogExcludeTypeEnums.values()).
            collect(Collectors.toMap(OperationLogExcludeTypeEnums::getCode, e -> e));

    private int code;
    private String detail;

    OperationLogExcludeTypeEnums(int code, String detail) {
        this.code = code;
        this.detail = detail;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public static OperationLogExcludeTypeEnums getByCode(int value) {
        return map.get(value);
    }
}
