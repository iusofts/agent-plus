package com.iusofts.agentplus.aiflow.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 节点运行状态枚举
 */
public enum NodeRunStatusEnum {

    NOT_EXECUTED(0, "未执行"),
    RUNNING(1, "执行中"),
    SUCCESS(2, "成功"),
    FAILED(3, "失败"),
    SKIPPED(4, "跳过");

    private static Map<Integer, NodeRunStatusEnum> map = Arrays.stream(NodeRunStatusEnum.values()).
            collect(Collectors.toMap(NodeRunStatusEnum::getCode, e -> e));

    private Integer code;
    private String detail;

    NodeRunStatusEnum(Integer code, String detail) {
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

    public static NodeRunStatusEnum getByCode(Integer value) {
        return map.get(value);
    }
}
