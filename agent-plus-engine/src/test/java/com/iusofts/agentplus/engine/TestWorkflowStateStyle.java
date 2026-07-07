package com.iusofts.agentplus.engine;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.state.AgentState;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

public class TestWorkflowStateStyle {

    // 模拟 WorkflowState
    static class TestState extends AgentState {
        public static final String CTX_KEY = "ctx";

        public TestState(Map<String, Object> initData) {
            super(initData);
        }

        public TestContext ctx() {
            return this.<TestContext>value(CTX_KEY)
                    .orElseThrow(() -> new IllegalStateException("Context not found"));
        }
    }

    // 模拟 ExecutionContext
    static class TestContext {
        public final Map<String, String> status = new ConcurrentHashMap<>();

        public void updateStatus(String nodeId, String s) {
            status.put(nodeId, s);
            System.out.println("=== " + nodeId + " status: " + s + " ===");
        }
    }

    @Test
    void testWorkflowStyle() throws Exception {
        StateGraph<TestState> graph = new StateGraph<>(TestState::new);

        TestContext ctx = new TestContext();

        // 创建节点 Action
        AsyncNodeAction<TestState> action = state -> CompletableFuture.supplyAsync(() -> {
            String nodeId = "node1"; // 模拟
            state.ctx().updateStatus(nodeId, "RUNNING");
            try {
                System.out.println("=== Executing node ===");
                Thread.sleep(50);
                state.ctx().updateStatus(nodeId, "SUCCESS");
            } catch (InterruptedException e) {
                state.ctx().updateStatus(nodeId, "FAILED");
                Thread.currentThread().interrupt();
            }
            return Collections.emptyMap();
        });

        graph.addNode("node1", action);
        graph.addEdge(START, "node1");
        graph.addEdge("node1", END);

        CompiledGraph<TestState> compiled = graph.compile();
        System.out.println("=== Invoking with ctx ===");

        Optional<TestState> result = compiled.invoke(Map.of(TestState.CTX_KEY, ctx));
        System.out.println("=== Done ===");
        System.out.println("Status map: " + ctx.status);
    }
}
