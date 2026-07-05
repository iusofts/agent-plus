package com.iusofts.agentplus.engine.context;

import com.iusofts.agentplus.aiflow.vo.workflow.config.EnvVar;
import com.iusofts.agentplus.aiflow.vo.workflow.config.WorkflowConfig;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工作流一次执行的运行时上下文。
 *
 * <p>持有全局输入、节点输出快照、状态表以及配置。线程安全,允许在批处理节点内并行读写。</p>
 *
 * @author Ivan
 */
@Getter
public class ExecutionContext {

    private final String runId;
    private final WorkflowConfig config;
    private final Map<String, Object> globalInputs;
    private final Map<String, Object> envVars;
    private final Map<String, NodeOutput> nodeOutputs = new ConcurrentHashMap<>();
    private final Map<String, NodeExecutionStatus> nodeStatus = new ConcurrentHashMap<>();

    public ExecutionContext(String runId,
                            WorkflowConfig config,
                            Map<String, Object> globalInputs) {
        this.runId = runId;
        this.config = config;
        this.globalInputs = globalInputs == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(globalInputs);
        this.envVars = buildEnvVars(config);
    }

    private static Map<String, Object> buildEnvVars(WorkflowConfig config) {
        Map<String, Object> map = new HashMap<>();
        if (config != null && config.getEnvVars() != null) {
            for (EnvVar v : config.getEnvVars()) {
                if (v.getName() != null) {
                    map.put(v.getName(), v.getDefaultValue());
                }
            }
        }
        return map;
    }

    public void putOutput(NodeOutput output) {
        nodeOutputs.put(output.getNodeId(), output);
    }

    public NodeOutput getOutput(String nodeId) {
        return nodeOutputs.get(nodeId);
    }

    public void updateStatus(String nodeId, NodeExecutionStatus status) {
        nodeStatus.put(nodeId, status);
    }

    public NodeExecutionStatus getStatus(String nodeId) {
        return nodeStatus.getOrDefault(nodeId, NodeExecutionStatus.PENDING);
    }

    public Map<String, NodeOutput> snapshotOutputs() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(nodeOutputs));
    }
}
