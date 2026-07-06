package com.iusofts.agentplus.aiflow.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 运行状态枚举
 */
public enum RunStatusEnum {

    WAITING(0, "等待"),
    RUNNING(1, "运行中"),
    SUCCESS(2, "成功"),
    FAILED(3, "失败"),
    TERMINATED(4, "终止");

    private static Map<Integer, RunStatusEnum> map = Arrays.stream(RunStatusEnum.values()).
            collect(Collectors.toMap(RunStatusEnum::getCode, e -> e));

    private Integer code;
    private String detail;

    RunStatusEnum(Integer code, String detail) {
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

    public static RunStatusEnum getByCode(Integer value) {
        return map.get(value);
    }
}
