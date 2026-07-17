package com.iusofts.agentplus.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iusofts.agentplus.aiflow.vo.workflow.Workflow;
import com.iusofts.agentplus.aiflow.vo.workflow.config.EnvVar;
import com.iusofts.agentplus.aiflow.vo.workflow.config.WorkflowConfig;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.mock.MockChatModelProvider;
import com.iusofts.agentplus.engine.mock.MockKnowledgeRetriever;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 批处理流程冒烟测试
 */
class WorkflowBatchSmokeTest {

    private static final String WORKFLOW_RESOURCE = "workflow-batch-sample.json";

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void runBatchWorkflow() throws Exception {
        Workflow workflow = loadWorkflow();
        System.out.println("=== Workflow nodes:");
        for (var node : workflow.getNodes()) {
            System.out.println("  node " + node.getId() + ", parentNode=" + node.getParentNode() + ", type=" + node.getType());
        }
        WorkflowConfig config = buildWorkflowConfig();
        Map<String, Object> inputs = Map.of(
                "query", "我想喝水",
                "ssss", List.of("a", "b", "c")
        );

        // 为了让 LLM 节点的自定义输出有值，我们需要定制 MockChatModelProvider
        MockChatModelProvider mockProvider = new MockChatModelProvider((data, messages) -> {
            // 如果是主流程的 LLM 节点，返回一个数组的 JSON
            if ("大模型".equals(data.getLabel())) {
                return "[\"喝水\", \"打游戏\"]";
            }
            // 批处理内部的 LLM 节点，正常返回文本
            return "[mock:1] 处理了：" + messages;
        });

        WorkflowEngine engine = WorkflowEngine.builder()
                .chatModelProvider(mockProvider)
                .knowledgeRetriever(new MockKnowledgeRetriever())
                .build();

        WorkflowExecutionResult result = engine.execute(workflow, config, inputs);

        System.out.println("=== runId: " + result.getRunId());
        System.out.println("=== final output: " + result.getOutput());
        System.out.println("=== node status: " + result.getNodeStatus());
        result.getNodeOutputs().forEach((id, out) ->
                System.out.println("--- node " + id + " → " + out.getOutputs()));

        // 也打印一下批处理节点的完整输出，看看 results 里有什么
        NodeOutput batchOutput = result.getNodeOutputs().get("node-Batch-1783390242671");
        if (batchOutput != null) {
            System.out.println("=== Batch node full output: " + batchOutput.getOutputs());
        }

        // 直接反射获取一下 result 里的 results 看看
        try {
            var field = result.getClass().getDeclaredField("results");
            field.setAccessible(true);
            var results = field.get(result);
            System.out.println("=== Raw results: " + results);
        } catch (Exception e) {
            System.out.println("=== Can't get raw results: " + e.getMessage());
        }

        assertNotNull(result.getRunId(), "runId 不应为空");
        assertNotNull(result.getOutput(), "final output 不应为空");
        assertNotNull(result.getNodeStatus(), "node status 不应为空");
        assertTrue(!result.getNodeStatus().isEmpty(), "至少应有一个节点被登记状态");
    }

    private Workflow loadWorkflow() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(WORKFLOW_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException(
                        "未找到工作流样例文件: src/test/resources/" + WORKFLOW_RESOURCE);
            }
            return mapper.readValue(in, Workflow.class);
        }
    }

    private WorkflowConfig buildWorkflowConfig() {
        WorkflowConfig config = new WorkflowConfig();
        List<EnvVar> envVars = new ArrayList<>();
        EnvVar envVar = new EnvVar();
        envVar.setName("type");
        envVar.setType("String");
        envVar.setDefaultValue("1");
        envVars.add(envVar);
        config.setEnvVars(envVars);
        return config;
    }
}
