package com.iusofts.agentplus.engine.graph;

import com.iusofts.agentplus.engine.context.ExecutionContext;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 跟踪 ExecutionContext 的工具类，通过 runId 在静态 Map 中查找原始 ctx，
 * 避免 langgraph4j 状态克隆导致 ctx 被复制的问题。
 *
 * @author Ivan
 */
public class ExecutionContextTracker implements Serializable {
    // 静态的 tracker，存储所有正在执行的 ctx，key 是 runId
    private static final Map<String, ExecutionContext> tracker = new ConcurrentHashMap<>();

    private final String runId;

    public ExecutionContextTracker(ExecutionContext ctx) {
        this.runId = ctx.getRunId();
        tracker.put(this.runId, ctx);
    }

    public ExecutionContext get() {
        return tracker.get(runId);
    }

    /**
     * 执行完毕后清理 tracker，防止内存泄漏
     */
    public static void remove(String runId) {
        tracker.remove(runId);
    }
}
