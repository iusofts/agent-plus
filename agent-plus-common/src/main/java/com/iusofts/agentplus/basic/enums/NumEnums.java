package com.iusofts.agentplus.basic.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 阿拉伯数字转汉字
 */
public enum NumEnums {
    ONE(1, "一"),
    TWO(2, "二"),
    THREE(3, "三"),
    FOUR(4, "四"),
    FIVE(5, "五"),
    SIX(6, "六"),
    SEVEN(7, "七"),
    EIGHT(8, "八"),
    NINE(9, "九"),
    TEN(10, "十");

    private static Map<Integer, NumEnums> map = Arrays.stream(NumEnums.values()).
            collect(Collectors.toMap(NumEnums::getCode, e -> e));

    private int code;
    private String detail;

    NumEnums(int code, String detail) {
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

    public static NumEnums getByCode(int value) {
        return map.get(value);
    }
}
