package com.iusofts.agentplus.engine.context;

import lombok.Getter;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 单个节点的执行时间记录。
 *
 * <p>由 {@code WorkflowGraphCompiler#wrapExecutor} 在每次节点执行前后写入 {@link ExecutionContext},
 * 供落库场景取真实的开始/结束/耗时。</p>
 *
 * @author Ivan
 */
@Getter
public class NodeTiming implements Serializable {

    private final String nodeId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final long costMs;

    public NodeTiming(String nodeId, LocalDateTime startTime, LocalDateTime endTime) {
        this.nodeId = nodeId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.costMs = (startTime != null && endTime != null)
                ? Duration.between(startTime, endTime).toMillis()
                : 0L;
    }
}
