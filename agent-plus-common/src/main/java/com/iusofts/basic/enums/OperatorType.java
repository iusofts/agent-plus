package com.iusofts.basic.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 操作人类别
 *
 * @author Ivan
 */
public enum OperatorType {
    OTHER(0, "其它"),

    MANAGE(1, "后台用户"),

    CLIENT(2, "客户端用户");

    private static Map<Integer, OperatorType> map = Arrays.stream(OperatorType.values()).
            collect(Collectors.toMap(OperatorType::getCode, e -> e));

    private int code;
    private String detail;

    OperatorType(int code, String detail) {
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

    public static OperatorType getByCode(int value) {
        return map.get(value);
    }
}
