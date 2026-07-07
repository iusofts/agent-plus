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

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

public class TestAsyncNodeAction {

    @Test
    void testAsyncAction() throws Exception {
        StateGraph<AgentState> graph = new StateGraph<>(AgentState::new);

        // 测试用 AsyncNodeAction - 模仿 WorkflowGraphCompiler 的写法
        AsyncNodeAction<AgentState> asyncAction = state -> CompletableFuture.supplyAsync(() -> {
            System.out.println("=== Executing async node ===");
            try {
                Thread.sleep(100); // 模拟一点工作
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return Collections.emptyMap();
        });

        graph.addNode("test", asyncAction);
        graph.addEdge(START, "test");
        graph.addEdge("test", END);

        CompiledGraph<AgentState> compiled = graph.compile();
        System.out.println("=== Invoking ===");

        Optional<AgentState> result = compiled.invoke(Map.of());
        System.out.println("=== Done ===");
        System.out.println("Result present: " + result.isPresent());
    }
}
