package com.iusofts.agentplus.engine.graph;

import com.iusofts.agentplus.engine.context.ExecutionContext;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 跟踪 ExecutionContext 的工具类，通过 runId 在静态 Map 中查找原始 ctx，
 * 避免 langgraph4j 状态克隆导致 ctx 被复制的问题。
 *
 * @author Ivan
 */
public class ExecutionContextTracker implements Serializable {
    // 静态的 tracker，存储所有正在执行的 ctx，key 是 runId#scopeKey 或 runId
    private static final Map<String, ExecutionContext> tracker = new ConcurrentHashMap<>();

    private final String key;

    public ExecutionContextTracker(ExecutionContext ctx) {
        this.key = keyOf(ctx.getRunId(), ctx.getScopeKey());
        tracker.put(this.key, ctx);
    }

    public ExecutionContext get() {
        return tracker.get(key);
    }

    /** tracker 的 key：无作用域用 runId，批处理作用域用 runId#scopeKey。 */
    public static String keyOf(String runId, String scopeKey) {
        return scopeKey == null ? runId : runId + "#" + scopeKey;
    }

    /**
     * 精确移除单个 ctx（批处理每轮迭代结束时调用）。
     *
     * <p>不能用前缀匹配：scopeKey 形如 {@code batchId#index}，
     * 前缀匹配会把 {@code ...#1} 误当作 {@code ...#10 / #11 / ...} 的前缀，
     * 从而在并行迭代时误删仍在运行的其他轮次的 ctx。</p>
     */
    public static void removeKey(String key) {
        tracker.remove(key);
    }

    /**
     * 主流程执行完毕后清理该 runId 下的全部 ctx（含所有批处理作用域），防止内存泄漏。
     *
     * <p>仅在整个 run 结束时调用，此时不存在并发迭代，前缀清理是安全的。
     * 前缀加上 {@code #} 分隔或精确等于 runId，避免误伤 runId 互为前缀的其他 run。</p>
     */
    public static void removeRun(String runId) {
        tracker.keySet().removeIf(k -> k.equals(runId) || k.startsWith(runId + "#"));
    }
}
