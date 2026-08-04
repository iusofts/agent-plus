package com.iusofts.agentplus.aiflow.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 结束节点回答模式枚举。
 *
 * <p>JSON 序列化为小写字符串值(variable / text),与历史数据保持兼容。</p>
 */
public enum AnswerModeEnum {

    /** 变量模式:返回所有 outputParams 的变量映射(原 JSON 形式)。 */
    VARIABLE("variable"),
    /** 文本模式:渲染 answerContent 模板得到完整文本,作为 outputs.text 返回。 */
    TEXT("text");

    private static final Map<String, AnswerModeEnum> map = Arrays.stream(AnswerModeEnum.values())
            .collect(Collectors.toMap(AnswerModeEnum::getValue, e -> e));

    private final String value;

    AnswerModeEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    /** 兼容历史字段(部分代码可能仍按 getName/getCode 读取)。 */
    public String getName() {
        return value;
    }

    @JsonCreator
    public static AnswerModeEnum fromValue(String value) {
        if (value == null) {
            return null;
        }
        AnswerModeEnum m = map.get(value);
        if (m == null) {
            throw new IllegalArgumentException("未知的回答模式: " + value);
        }
        return m;
    }

    public static AnswerModeEnum getByValue(String value) {
        return value == null ? null : map.get(value);
    }
}
