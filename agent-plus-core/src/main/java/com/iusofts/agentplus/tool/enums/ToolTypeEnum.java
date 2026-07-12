package com.iusofts.agentplus.tool.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 工具类型枚举.
 *
 * @author Ivan
 */
@Getter
@RequiredArgsConstructor
public enum ToolTypeEnum {

    BUILT_IN(1, "内置工具"),
    CUSTOM(2, "自定义工具");

    private final Integer code;
    private final String desc;

    public static ToolTypeEnum fromCode(Integer code) {
        for (ToolTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
