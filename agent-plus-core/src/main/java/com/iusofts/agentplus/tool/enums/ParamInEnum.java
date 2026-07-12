package com.iusofts.agentplus.tool.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 参数传入方式枚举.
 *
 * @author Ivan
 */
@Getter
@RequiredArgsConstructor
public enum ParamInEnum {

    BODY("Body", "Body"),
    PATH("Path", "Path"),
    QUERY("Query", "Query"),
    HEADER("Header", "Header");

    private final String label;
    private final String value;

    public static ParamInEnum fromValue(String value) {
        for (ParamInEnum inEnum : values()) {
            if (inEnum.getValue().equals(value)) {
                return inEnum;
            }
        }
        return null;
    }
}
