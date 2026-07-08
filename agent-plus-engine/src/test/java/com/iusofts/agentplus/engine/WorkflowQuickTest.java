package com.iusofts.agentplus.engine;

import com.iusofts.agentplus.aiflow.vo.workflow.Edge;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.Workflow;
import com.iusofts.agentplus.aiflow.vo.workflow.config.Model;
import com.iusofts.agentplus.aiflow.vo.workflow.data.EndNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.LLMNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.StartNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.InputParam;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import com.iusofts.agentplus.engine.config.WorkflowEngineAutoConfiguration;
import com.iusofts.agentplus.engine.llm.DoubaoProperties;
import com.iusofts.agentplus.engine.llm.QwenProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工作流快速测试类。
 *
 * <p>使用前请确保配置了环境变量 DASHSCOPE_API_KEY 或在下方直接设置 API Key。</p>
 *
 * @author Ivan
 */
public class WorkflowQuickTest {

    /**
     * 如果不想配置环境变量，可以在这里直接设置 API Key.
     */
    private static final String API_KEY = null; // "your-api-key-here";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WorkflowEngineAutoConfiguration.class))
            .withPropertyValues(
                    "dashscope.api-key=" + (API_KEY != null ? API_KEY : System.getenv().getOrDefault("DASHSCOPE_API_KEY", "")),
                    "dashscope.model=qwen-plus"
            );

    /**
     * 测试简单问答工作流。
     */
    @Test
    public void testSimpleChatWorkflow() {
        contextRunner.run(context -> {
            WorkflowEngine engine = context.getBean(WorkflowEngine.class);
            assertNotNull(engine);

            Workflow workflow = buildSimpleChatWorkflow();

            Map<String, Object> inputs = Map.of(
                    "question", "什么是 Agent AI？请用3句话解释。"
            );

            WorkflowExecutionResult result = engine.execute(workflow, inputs);
            assertNotNull(result);
            assertTrue(result.isSuccess());

            System.out.println("=== 执行结果 ===");
            System.out.println("状态: " + result.getStatus());
            System.out.println("输出: " + result.getOutputs());

            if (result.getError() != null) {
                System.err.println("错误: " + result.getError());
            }
        });
    }

    /**
     * 构建一个简单的问答工作流: Start -> LLM -> End.
     */
    private Workflow buildSimpleChatWorkflow() {
        Workflow workflow = new Workflow();
        workflow.setId("test-simple-chat");
        workflow.setName("简单问答测试");

        // Start 节点
        Node startNode = new Node();
        startNode.setId("start");
        startNode.setType("start");

        StartNodeData startData = new StartNodeData();
        startData.setInputParams(List.of(
                inputParam("question", "用户问题", "string")
        ));
        startNode.setData(startData);

        // LLM 节点
        Node llmNode = new Node();
        llmNode.setId("llm");
        llmNode.setType("llm");

        LLMNodeData llmData = new LLMNodeData();
        llmData.setModel("qwen-plus");
        llmData.setSystemPrompt("你是一个乐于助人的AI助手。");
        llmData.setUserPrompt("{{question}}");
        llmData.setTemperature(0.7);
        llmData.setOutputParams(List.of(
                outputParam("answer", "回答", "string")
        ));

        Model modelConfig = new Model();
        modelConfig.setProvider("qwen");
        modelConfig.setName("qwen-plus");
        llmData.setModelConfig(modelConfig);

        llmNode.setData(llmData);

        // End 节点
        Node endNode = new Node();
        endNode.setId("end");
        endNode.setType("end");

        EndNodeData endData = new EndNodeData();
        endData.setOutputParams(List.of(
                outputParam("result", "最终结果", "{{llm.answer}}")
        ));
        endNode.setData(endData);

        workflow.setNodes(List.of(startNode, llmNode, endNode));

        // 边
        Edge edge1 = new Edge();
        edge1.setId("e1");
        edge1.setSource("start");
        edge1.setTarget("llm");

        Edge edge2 = new Edge();
        edge2.setId("e2");
        edge2.setSource("llm");
        edge2.setTarget("end");

        workflow.setEdges(List.of(edge1, edge2));

        return workflow;
    }

    private InputParam inputParam(String name, String label, String type) {
        InputParam param = new InputParam();
        param.setName(name);
        param.setLabel(label);
        param.setType(type);
        return param;
    }

    private OutputParam outputParam(String name, String label, Object value) {
        OutputParam param = new OutputParam();
        param.setName(name);
        param.setLabel(label);
        param.setValue(value);
        return param;
    }
}
