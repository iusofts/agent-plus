package com.iusofts.agentplus.aiflow.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 流程执行结果DTO。
 *
 * @author Ivan Shen
 */
@Data
public class FlowExecuteResult {
    private Long runtimeId;
    private String traceId;
    private Long flowId;
    private Integer runStatus;
    private Map<String, Object> output;
    private Long costMs;
    private String errorMsg;
    private List<FlowNodeResult> nodeResults;

    /**
     * 节点执行结果DTO。
     */
    @Data
    public static class FlowNodeResult {
        private String nodeId;
        private String nodeType;
        private Integer runStatus;
        private Map<String, Object> output;
        private Long costMs;
        private String errorStack;
    }
}
