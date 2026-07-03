package com.iusofts.agentplus.ai.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 类型 1.问候型 2.销售型 3.鉴别型
 */
public enum AiAgentType {

    WELCOME(1, "问候型"),
    SALER(2, "销售型"),
    RESOLVING(3, "鉴别型");
    ;

    private static Map<Integer, AiAgentType> map = Arrays.stream(AiAgentType.values()).
            collect(Collectors.toMap(AiAgentType::getCode, e -> e));

    private int code;
    private String detail;

    AiAgentType(int code, String detail) {
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

    public static AiAgentType getByCode(int value) {
        return map.get(value);
    }
}
