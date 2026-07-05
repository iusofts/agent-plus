package com.iusofts.agentplus.engine.executor;

import com.iusofts.agentplus.engine.exception.WorkflowExecutionException;

import java.util.HashMap;
import java.util.Map;

/**
 * 节点执行器注册表。按 {@code Node.type} 查找对应实现。
 *
 * @author Ivan
 */
public class NodeExecutorRegistry {

    private final Map<String, NodeExecutor> executors = new HashMap<>();

    public NodeExecutorRegistry register(NodeExecutor executor) {
        executors.put(executor.type().toLowerCase(), executor);
        return this;
    }

    public NodeExecutor get(String type) {
        if (type == null) {
            throw new WorkflowExecutionException("节点 type 为空");
        }
        NodeExecutor executor = executors.get(type.toLowerCase());
        if (executor == null) {
            throw new WorkflowExecutionException("未注册的节点类型: " + type);
        }
        return executor;
    }
}
