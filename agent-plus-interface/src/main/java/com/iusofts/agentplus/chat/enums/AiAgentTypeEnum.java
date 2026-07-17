package com.iusofts.agentplus.chat.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 智能体类型枚举
 */
public enum AiAgentTypeEnum {

    AUTONOMOUS(1, "自主规划"),
    CHATFLOW(2, "对话流");

    private static Map<Integer, AiAgentTypeEnum> map = Arrays.stream(AiAgentTypeEnum.values()).
            collect(Collectors.toMap(AiAgentTypeEnum::getCode, e -> e));

    private Integer code;
    private String detail;

    AiAgentTypeEnum(Integer code, String detail) {
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

    public static AiAgentTypeEnum getByCode(Integer value) {
        return map.get(value);
    }
}
