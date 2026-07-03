package com.iusofts.agentplus.aiflow.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流程启用状态枚举
 */
public enum FlowStatusEnum {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private static Map<Integer, FlowStatusEnum> map = Arrays.stream(FlowStatusEnum.values()).
            collect(Collectors.toMap(FlowStatusEnum::getCode, e -> e));

    private Integer code;
    private String detail;

    FlowStatusEnum(Integer code, String detail) {
        this.code = code;
        this.detail = detail;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public static FlowStatusEnum getByCode(Integer value) {
        return map.get(value);
    }
}
