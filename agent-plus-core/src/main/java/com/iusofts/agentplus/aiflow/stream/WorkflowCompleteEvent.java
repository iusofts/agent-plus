package com.iusofts.agentplus.aiflow.stream;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * 工作流完成流式事件。
 *
 * @author Ivan Shen
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class WorkflowCompleteEvent extends WorkflowStreamEvent {
    /** 最终输出结果 */
    private Map<String, Object> output;
    /** 运行ID (traceId) */
    private String traceId;

    public static WorkflowCompleteEvent create(String runId, Map<String, Object> output) {
        WorkflowCompleteEvent event = new WorkflowCompleteEvent();
        event.setType(WorkflowStreamEventType.WORKFLOW_COMPLETE.value());
        event.setRunId(runId);
        event.setTraceId(runId);
        event.setOutput(output);
        event.setTimestamp(java.time.Instant.now().toEpochMilli());
        return event;
    }
}