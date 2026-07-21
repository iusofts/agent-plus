package com.iusofts.agentplus.aiflow.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iusofts.agentplus.aiflow.vo.workflow.Workflow;
import com.iusofts.agentplus.aiflow.vo.workflow.config.WorkflowConfig;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;

import java.util.UUID;

/**
 * AI流程公共工具方法。
 * 抽离试运行与对话流执行中的公共代码。
 *
 * @author Ivan Shen
 */
public class AiFlowCommonUtils {

    /**
     * 生成新的追踪ID。
     * 试运行会加上 trial- 前缀。
     */
    public static String newTraceId(boolean isTrial) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return isTrial ? "trial-" + uuid : uuid;
    }

    /**
     * 生成普通追踪ID（无前缀）。
     */
    public static String newTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 反序列化工作流定义。
     */
    public static Workflow deserializeWorkflow(String flowJson, ObjectMapper objectMapper) {
        if (flowJson == null || flowJson.isBlank()) {
            throw new SystemBusinessException("流程定义为空，无法运行");
        }
        try {
            return objectMapper.readValue(flowJson, Workflow.class);
        } catch (JsonProcessingException e) {
            throw new SystemBusinessException("流程数据解析失败");
        }
    }

    /**
     * 反序列化工作流配置。
     */
    public static WorkflowConfig deserializeConfig(String configJson, ObjectMapper objectMapper) {
        if (configJson == null || configJson.isBlank()) {
            return new WorkflowConfig();
        }
        try {
            return objectMapper.readValue(configJson, WorkflowConfig.class);
        } catch (JsonProcessingException e) {
            throw new SystemBusinessException("流程配置数据解析失败");
        }
    }

    /**
     * 序列化对象为 JSON 字符串。
     */
    public static String serialize(Object value, ObjectMapper objectMapper) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 截断文本（用于错误信息落库长度保护）。
     */
    public static String truncate(String text) {
        if (text == null) {
            return null;
        }
        int max = 2000;
        return text.length() > max ? text.substring(0, max) : text;
    }
}
