package com.iusofts.agentplus.aiflow.stream;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * 工作流流式事件基类。
 *
 * @author Ivan Shen
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class WorkflowStreamEvent {
    /** 事件类型 */
    private String type;
    /** 运行ID (traceId) */
    private String runId;
    /** 时间戳 */
    private long timestamp;

    protected WorkflowStreamEvent(String type, String runId) {
        this.type = type;
        this.runId = runId;
        this.timestamp = Instant.now().toEpochMilli();
    }
}