package com.iusofts.agentplus.engine.graph;

import com.iusofts.agentplus.engine.context.ExecutionContext;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;

/**
 * langgraph4j 状态图携带的状态对象。
 *
 * <p>只持有一个 slot: {@link #CTX_KEY},指向执行期共享的 {@link ExecutionContext}。
 * 节点动作与条件路由都通过该 ctx 读写运行时数据(节点输出、节点状态、条件命中分支等),
 * 因此 langgraph4j 侧不需要为每个字段声明 Channel,状态合并语义交给 ctx 内部的
 * 并发结构保证。
 *
 * @author Ivan
 */
public class WorkflowState extends AgentState {

    public static final String CTX_KEY = "ctx";

    public WorkflowState(Map<String, Object> initData) {
        super(initData);
    }

    public ExecutionContext ctx() {
        Object value = data().get(CTX_KEY);
        if (value instanceof ExecutionContextTracker) {
            ExecutionContext ctx = ((ExecutionContextTracker) value).get();
            if (ctx == null) {
                throw new IllegalStateException("无法找到 runId=" + ((ExecutionContextTracker) value) + " 对应的 ExecutionContext");
            }
            return ctx;
        } else if (value instanceof ExecutionContext) {
            return (ExecutionContext) value;
        }
        throw new IllegalStateException("ExecutionContext 未注入 WorkflowState，value=" + value);
    }
}
