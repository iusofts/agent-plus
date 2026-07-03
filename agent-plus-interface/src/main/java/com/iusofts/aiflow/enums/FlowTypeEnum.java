package com.iusofts.aiflow.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流程类型枚举
 */
public enum FlowTypeEnum {

    WORKFLOW(1, "工作流Workflow"),
    CHATFLOW(2, "对话流Chatflow");

    private static Map<Integer, FlowTypeEnum> map = Arrays.stream(FlowTypeEnum.values()).
            collect(Collectors.toMap(FlowTypeEnum::getCode, e -> e));

    private Integer code;
    private String detail;

    FlowTypeEnum(Integer code, String detail) {
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

    public static FlowTypeEnum getByCode(Integer value) {
        return map.get(value);
    }
}
