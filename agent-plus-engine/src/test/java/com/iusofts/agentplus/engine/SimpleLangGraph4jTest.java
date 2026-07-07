package com.iusofts.agentplus.engine;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncNodeActionWithConfig;
import org.bsc.langgraph4j.state.AgentState;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeActionWithConfig.node_async;

public class SimpleLangGraph4jTest {

    @Test
    void testSimpleGraph() throws Exception {
        // 创建一个简单的图
        StateGraph<AgentState> graph = new StateGraph<>(AgentState::new);

        // 添加节点
        AsyncNodeActionWithConfig<AgentState> nodeAction = node_async((state, config) -> {
            System.out.println("=== Executing node: " + config.nodeId() + " ===");
            System.out.println("Input state: " + state.data());
            return Map.of("result", "hello from " + config.nodeId());
        });

        graph.addNode("start", nodeAction);
        graph.addNode("middle", nodeAction);
        graph.addNode("end", nodeAction);

        // 添加边
        graph.addEdge(START, "start");
        graph.addEdge("start", "middle");
        graph.addEdge("middle", "end");
        graph.addEdge("end", END);

        // 编译并运行
        CompiledGraph<AgentState> compiled = graph.compile();
        System.out.println("=== Graph compiled ===");

        Optional<AgentState> result = compiled.invoke(Map.of("input", "test"));
        System.out.println("=== Result ===");
        System.out.println("Present: " + result.isPresent());
        if (result.isPresent()) {
            System.out.println("State: " + result.get().data());
        }
    }
}
