package com.iusofts.agentplus.basic.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum DeleteFlagEnums {

    NO(0, "未删除"),
    YES(1, "已删除");

    private static Map<Integer, DeleteFlagEnums> map = Arrays.stream(DeleteFlagEnums.values()).
            collect(Collectors.toMap(DeleteFlagEnums::getCode, e -> e));

    private int code;
    private String detail;

    DeleteFlagEnums(int code, String detail) {
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

    public static DeleteFlagEnums getByCode(int value) {
        return map.get(value);
    }
}
