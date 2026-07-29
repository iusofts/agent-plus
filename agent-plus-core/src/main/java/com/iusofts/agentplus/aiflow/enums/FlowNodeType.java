package com.iusofts.agentplus.aiflow.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流程节点类型枚举
 */
public enum FlowNodeType {

    START("Start", "开始"),
    LLM("LLM", "大模型"),
    KNOWLEDGE("Knowledge", "知识库"),
    TOOL("Tool", "工具"),
    CONDITION("Condition", "选择器"),
    BATCH("Batch", "批处理"),
    AGGREGATOR("Aggregator", "变量聚合"),
    CODE("Code", "代码"),
    END("End", "结束");

    private static Map<String, FlowNodeType> map = Arrays.stream(FlowNodeType.values()).
            collect(Collectors.toMap(FlowNodeType::getCode, e -> e));

    private String code;
    private String detail;

    FlowNodeType(String code, String detail) {
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

    public static FlowNodeType getByCode(String value) {
        return map.get(value);
    }
}