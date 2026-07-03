package com.iusofts.ai.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 业务类型 0.测试 1.默认应用  
 */
public enum AiBusinessType {

    TEST(0, "测试"),
    DEFAULT(1, "默认应用"),
    ;

    private static Map<Integer, AiBusinessType> map = Arrays.stream(AiBusinessType.values()).
            collect(Collectors.toMap(AiBusinessType::getCode, e -> e));

    private int code;
    private String detail;

    AiBusinessType(int code, String detail) {
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

    public static AiBusinessType getByCode(int value) {
        return map.get(value);
    }
}
