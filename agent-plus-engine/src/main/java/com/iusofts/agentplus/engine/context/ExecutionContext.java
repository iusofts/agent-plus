package com.iusofts.agentplus.engine.context;

import com.iusofts.agentplus.aiflow.vo.workflow.config.EnvVar;
import com.iusofts.agentplus.aiflow.vo.workflow.config.WorkflowConfig;
import lombok.Getter;
import org.bsc.langgraph4j.CompiledGraph;

import java.io.Serializable;
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
 * <p>支持子作用域({@link #newScope(String)}):批处理节点每轮迭代创建独立作用域,
 * 拥有自己的 outputs/status,读取时未命中会回退到父作用域,保证子图节点仍可引用
 * batch 之外的上游数据,但每轮迭代之间互不干扰。</p>
 *
 * @author Ivan
 */
@Getter
public class ExecutionContext implements Serializable {

    private final String runId;
    private final WorkflowConfig config;
    private final Map<String, Object> globalInputs;
    private final Map<String, Object> envVars;
    private final Map<String, NodeOutput> nodeOutputs = new ConcurrentHashMap<>();
    private final Map<String, NodeExecutionStatus> nodeStatus = new ConcurrentHashMap<>();
    private transient final ExecutionContext parent;
    private final String scopeKey;

    /** 主图运行时可用的批处理子图,由 {@code WorkflowGraphCompiler} 预编译写入,BatchNodeExecutor 直接调用。 */
    private transient final Map<String, CompiledGraph<?>> batchSubGraphs = new ConcurrentHashMap<>();

    public ExecutionContext(String runId,
                            WorkflowConfig config,
                            Map<String, Object> globalInputs) {
        this.runId = runId;
        this.config = config;
        this.globalInputs = globalInputs == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(globalInputs);
        this.envVars = buildEnvVars(config);
        this.parent = null;
        this.scopeKey = null;
    }

    private ExecutionContext(ExecutionContext parent, String scopeKey) {
        this.runId = parent.runId;
        this.config = parent.config;
        this.globalInputs = parent.globalInputs;
        this.envVars = parent.envVars;
        this.parent = parent;
        this.scopeKey = scopeKey;
        this.batchSubGraphs.putAll(parent.batchSubGraphs);
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

    public ExecutionContext newScope(String scopeKey) {
        return new ExecutionContext(this, scopeKey);
    }

    public void putOutput(NodeOutput output) {
        nodeOutputs.put(output.getNodeId(), output);
    }

    public NodeOutput getOutput(String nodeId) {
        NodeOutput local = nodeOutputs.get(nodeId);
        if (local != null) {
            return local;
        }
        return parent == null ? null : parent.getOutput(nodeId);
    }

    public void updateStatus(String nodeId, NodeExecutionStatus status) {
        nodeStatus.put(nodeId, status);
    }

    public NodeExecutionStatus getStatus(String nodeId) {
        NodeExecutionStatus local = nodeStatus.get(nodeId);
        if (local != null) {
            return local;
        }
        return parent == null
                ? NodeExecutionStatus.PENDING
                : parent.getStatus(nodeId);
    }

    public Map<String, NodeOutput> snapshotOutputs() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(nodeOutputs));
    }

    public void registerBatchSubGraph(String batchId, CompiledGraph<?> subGraph) {
        batchSubGraphs.put(batchId, subGraph);
    }

    public CompiledGraph<?> getBatchSubGraph(String batchId) {
        return batchSubGraphs.get(batchId);
    }
}
