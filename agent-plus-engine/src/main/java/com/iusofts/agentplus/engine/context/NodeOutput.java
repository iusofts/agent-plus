package com.iusofts.agentplus.engine.context;

import lombok.Getter;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 单个节点的执行产物。
 *
 * <p>{@link #outputs} 中的键对应 {@code OutputParam.name},下游节点通过
 * {@code InputParam.paramMapKey} 引用。</p>
 *
 * <p>{@link #chosenBranch} 仅条件节点会填充,为命中分支的 {@code condition.id},
 * 调度器据此对下游边做剪枝。</p>
 *
 * @author Ivan
 */
@Getter
public class NodeOutput implements Serializable {

    private final String nodeId;
    private final Map<String, Object> outputs;
    private final String chosenBranch;

    public NodeOutput(String nodeId, Map<String, Object> outputs) {
        this(nodeId, outputs, null);
    }

    public NodeOutput(String nodeId, Map<String, Object> outputs, String chosenBranch) {
        this.nodeId = nodeId;
        this.outputs = outputs == null ? new LinkedHashMap<>() : outputs;
        this.chosenBranch = chosenBranch;
    }

    public static NodeOutput empty(String nodeId) {
        return new NodeOutput(nodeId, Collections.emptyMap());
    }
}
