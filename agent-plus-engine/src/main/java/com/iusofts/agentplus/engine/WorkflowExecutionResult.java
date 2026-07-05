package com.iusofts.agentplus.engine;

import com.iusofts.agentplus.engine.context.NodeExecutionStatus;
import com.iusofts.agentplus.engine.context.NodeOutput;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * 工作流一次执行的最终结果。
 *
 * @author Ivan
 */
@Getter
@RequiredArgsConstructor
public class WorkflowExecutionResult {

    private final String runId;
    private final Map<String, Object> output;
    private final Map<String, NodeOutput> nodeOutputs;
    private final Map<String, NodeExecutionStatus> nodeStatus;
}
