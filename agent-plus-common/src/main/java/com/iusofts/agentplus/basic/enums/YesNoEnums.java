package com.iusofts.agentplus.basic.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum YesNoEnums {

    NO(0, "否"),
    YES(1, "是");

    private static Map<Integer, YesNoEnums> map = Arrays.stream(YesNoEnums.values()).
            collect(Collectors.toMap(YesNoEnums::getCode, e -> e));

    private int code;
    private String detail;

    YesNoEnums(int code, String detail) {
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

    public static YesNoEnums getByCode(int value) {
        return map.get(value);
    }
}
