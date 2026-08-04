package com.iusofts.agentplus.engine.context;

import com.iusofts.agentplus.aiflow.enums.FlowTypeEnum;
import com.iusofts.agentplus.aiflow.vo.workflow.config.EnvVar;
import com.iusofts.agentplus.aiflow.vo.workflow.config.WorkflowConfig;
import com.iusofts.agentplus.aiflow.stream.WorkflowStreamEvent;
import com.iusofts.agentplus.engine.stream.WorkflowStreamEventCallback;
import lombok.Getter;
import lombok.Setter;
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
    /** 节点执行的起止时间,由 wrapExecutor 记录,供落库/查询取真实值。 */
    private final Map<String, NodeTiming> nodeTimings = new ConcurrentHashMap<>();
    private transient final ExecutionContext parent;
    private final String scopeKey;

    /** 关联的流程 ID,用于 AI 日志的 fromFlow 记录。 */
    private final Long flowId;
    /** 触发用户 ID,用于 AI 日志的 operator 记录。 */
    private final Long operatorId;
    /** 所属组织 ID,用于 AI 日志的 operator 记录。 */
    private final Integer orgId;
    /** 流程类型(工作流/对话流),由 WorkflowEngine 写入,供节点执行器区分对话流场景。 */
    private final FlowTypeEnum flowType;

    /** 主图运行时可用的批处理子图,由 {@code WorkflowGraphCompiler} 预编译写入,BatchNodeExecutor 直接调用。 */
    private transient final Map<String, CompiledGraph<?>> batchSubGraphs = new ConcurrentHashMap<>();

    /** Root span 的 Context，用于节点 span 的父 context，确保所有节点都是平级。 */
    private transient io.opentelemetry.context.Context rootContext;

    /** 流式事件回调（仅根 context 持有） */
    @Getter
    @Setter
    private transient WorkflowStreamEventCallback eventCallback;

    /** 是否为流式执行（仅根 context 持有） */
    @Getter
    @Setter
    private transient boolean streamingExecution;

    /** 节点ID -> 节点名称/类型 映射（仅根 context 持有） */
    private transient Map<String, String> nodeNameMap;
    private transient Map<String, String> nodeTypeMap;

    public ExecutionContext(String runId,
                            WorkflowConfig config,
                            Map<String, Object> globalInputs) {
        this(runId, config, globalInputs, null, null, null, null);
    }

    public ExecutionContext(String runId,
                            WorkflowConfig config,
                            Map<String, Object> globalInputs,
                            Long flowId,
                            Long operatorId,
                            Integer orgId) {
        this(runId, config, globalInputs, flowId, operatorId, orgId, null);
    }

    public ExecutionContext(String runId,
                            WorkflowConfig config,
                            Map<String, Object> globalInputs,
                            Long flowId,
                            Long operatorId,
                            Integer orgId,
                            FlowTypeEnum flowType) {
        this.runId = runId;
        this.config = config;
        this.globalInputs = globalInputs == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(globalInputs);
        this.envVars = buildEnvVars(config);
        this.parent = null;
        this.scopeKey = null;
        this.flowId = flowId;
        this.operatorId = operatorId;
        this.orgId = orgId;
        this.flowType = flowType;
    }

    private ExecutionContext(ExecutionContext parent, String scopeKey) {
        this.runId = parent.runId;
        this.config = parent.config;
        this.globalInputs = parent.globalInputs;
        this.envVars = parent.envVars;
        this.parent = parent;
        this.scopeKey = scopeKey;
        this.flowId = parent.flowId;
        this.operatorId = parent.operatorId;
        this.orgId = parent.orgId;
        this.flowType = parent.flowType;
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

    /** 记录节点起止时间,批处理子作用域也向根 ctx 写入,便于最终汇总。 */
    public void recordTiming(String nodeId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        NodeTiming timing = new NodeTiming(nodeId, startTime, endTime);
        ExecutionContext root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        root.nodeTimings.put(nodeId, timing);
    }

    /** 主 ctx 的节点起止时间快照;子作用域递归回退到根。 */
    public Map<String, NodeTiming> snapshotTimings() {
        ExecutionContext root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(root.nodeTimings));
    }

    public void registerBatchSubGraph(String batchId, CompiledGraph<?> subGraph) {
        batchSubGraphs.put(batchId, subGraph);
    }

    public CompiledGraph<?> getBatchSubGraph(String batchId) {
        return batchSubGraphs.get(batchId);
    }

    public io.opentelemetry.context.Context getRootContext() {
        ExecutionContext root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        return root.rootContext;
    }

    public void setRootContext(io.opentelemetry.context.Context rootContext) {
        ExecutionContext root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        root.rootContext = rootContext;
    }

    /** 设置节点名称和类型映射 */
    public void setNodeInfoMaps(Map<String, String> nodeNameMap, Map<String, String> nodeTypeMap) {
        ExecutionContext root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        root.nodeNameMap = nodeNameMap;
        root.nodeTypeMap = nodeTypeMap;
    }

    /** 获取节点名称 */
    public String getNodeName(String nodeId) {
        ExecutionContext root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        return root.nodeNameMap != null ? root.nodeNameMap.get(nodeId) : null;
    }

    /** 获取节点类型 */
    public String getNodeType(String nodeId) {
        ExecutionContext root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        return root.nodeTypeMap != null ? root.nodeTypeMap.get(nodeId) : null;
    }

    /** 推送流式事件（委托给根 context 的 callback） */
    public void emitEvent(WorkflowStreamEvent event) {
        ExecutionContext root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        if (root.eventCallback != null) {
            root.eventCallback.onEvent(event);
        }
    }
}
