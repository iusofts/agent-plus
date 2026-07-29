package com.iusofts.agentplus.aiflow.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 异常处理方式枚举
 */
public enum ErrorHandlingEnum {

    STOP("stop", "中断流程"),
    CUSTOM("custom", "返回设定内容"),
    EXCEPTION("exception", "执行异常流程");

    private static Map<String, ErrorHandlingEnum> map = Arrays.stream(ErrorHandlingEnum.values()).
            collect(Collectors.toMap(ErrorHandlingEnum::getCode, e -> e));

    private String code;
    private String detail;

    ErrorHandlingEnum(String code, String detail) {
        this.code = code;
        this.detail = detail;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public static ErrorHandlingEnum getByCode(String value) {
        return map.get(value);
    }
}
