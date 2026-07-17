package com.iusofts.agentplus.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iusofts.agentplus.aiflow.vo.workflow.Workflow;
import com.iusofts.agentplus.aiflow.vo.workflow.config.WorkflowConfig;
import com.iusofts.agentplus.engine.mock.MockChatModelProvider;
import com.iusofts.agentplus.engine.mock.MockKnowledgeRetriever;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 端到端冒烟测试。
 *
 * <p>准备工作:</p>
 * <ol>
 *   <li>把工作流 JSON 放到 <code>src/test/resources/workflow-sample.json</code>(必需)</li>
 *   <li>可选:把入参 JSON 放到 <code>src/test/resources/workflow-inputs.json</code>,
 *       内容为一个 <code>{"key": value}</code> 对象;不存在时使用空入参。</li>
 * </ol>
 *
 * <p>运行:</p>
 * <pre>mvn -pl agent-plus-engine -am test -Dtest=WorkflowSmokeTest</pre>
 *
 * <p>本用例仅做冒烟:</p>
 * <ul>
 *   <li>验证 langgraph4j 编译 + 主图 + batch 子图能被正确装配;</li>
 *   <li>验证 Start / Condition / LLM(mock) / Knowledge(mock) / Aggregator / Batch / End 各路径连通;</li>
 *   <li>把执行结果、每节点状态和产物打印到控制台便于人工核对。</li>
 * </ul>
 */
class WorkflowSmokeTest {

    private static final String WORKFLOW_RESOURCE = "workflow-sample.json";
    private static final String INPUTS_RESOURCE = "workflow-inputs.json";

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void runSampleWorkflow() throws Exception {
        Workflow workflow = loadWorkflow();
        Map<String, Object> inputs = loadInputs();

        WorkflowEngine engine = WorkflowEngine.builder()
                .chatModelProvider(new MockChatModelProvider())
                .knowledgeRetriever(new MockKnowledgeRetriever())
                .build();

        WorkflowExecutionResult result = engine.execute(workflow, new WorkflowConfig(), inputs);

        System.out.println("=== runId: " + result.getRunId());
        System.out.println("=== final output: " + result.getOutput());
        System.out.println("=== node status: " + result.getNodeStatus());
        result.getNodeOutputs().forEach((id, out) ->
                System.out.println("--- node " + id + " → " + out.getOutputs()));

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

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadInputs() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(INPUTS_RESOURCE)) {
            if (in == null) {
                return new LinkedHashMap<>();
            }
            Map<String, Object> map = mapper.readValue(in, Map.class);
            return map == null ? Collections.emptyMap() : map;
        }
    }
}
