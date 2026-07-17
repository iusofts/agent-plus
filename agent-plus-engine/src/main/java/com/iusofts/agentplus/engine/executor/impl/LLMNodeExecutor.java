package com.iusofts.agentplus.engine.executor.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import com.iusofts.agentplus.aiflow.vo.workflow.data.llm.LLMNodeData;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.exception.WorkflowExecutionException;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.llm.ChatModelProvider;
import com.iusofts.agentplus.engine.util.ParamResolver;
import com.iusofts.agentplus.llm.LlmModelQueryProvider;
import com.iusofts.agentplus.llm.dto.LlmModelConfigDTO;
import com.iusofts.agentplus.llm.dto.LlmModelDTO;
import com.iusofts.agentplus.llm.log.LlmLogRecorder;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * LLM 节点执行器,基于 LangChain4j 的 {@link ChatModel} 调用。
 *
 * <p>流程:</p>
 * <ol>
 *   <li>解析 {@code inputParams} 得到用户上下文,拼装 UserMessage。</li>
 *   <li>{@code systemPrompt} 支持 {@code {{node.name}}} 占位符渲染。</li>
 *   <li>调用模型,失败按 {@code retryCount} 重试。</li>
 *   <li>按 {@code errorHandling} 策略处理最终失败: {@code throw}(默认)/{@code custom}/{@code continue}。</li>
 *   <li>输出映射: 单输出直接放模型文本;多输出尝试解析 JSON,失败则全部放同一 key。</li>
 * </ol>
 *
 * <p>若引擎构造时提供了 {@link LlmLogRecorder} 与 {@link LlmModelQueryProvider},
 * 每次模型调用后会写一条 {@code ai_llm_call_log},含消息体与 token 使用量。</p>
 *
 * @author Ivan
 */
public class LLMNodeExecutor implements NodeExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LLMNodeExecutor.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final ChatModelProvider chatModelProvider;
    private final LlmLogRecorder llmLogRecorder;
    private final LlmModelQueryProvider modelQueryProvider;

    public LLMNodeExecutor(ChatModelProvider chatModelProvider) {
        this(chatModelProvider, null, null);
    }

    public LLMNodeExecutor(ChatModelProvider chatModelProvider,
                           LlmLogRecorder llmLogRecorder,
                           LlmModelQueryProvider modelQueryProvider) {
        this.chatModelProvider = chatModelProvider;
        this.llmLogRecorder = llmLogRecorder;
        this.modelQueryProvider = modelQueryProvider;
    }

    @Override
    public FlowNodeType type() {
        return FlowNodeType.LLM;
    }

    @Override
    public NodeOutput execute(Node node, ExecutionContext ctx) {
        LLMNodeData data = (LLMNodeData) node.getData();
        if (data == null) {
            throw new WorkflowExecutionException(node.getId(), "LLM 节点缺少 data", null);
        }

        Map<String, Object> inputs = ParamResolver.resolveInputs(data.getInputParams(), ctx);
        String systemPrompt = ParamResolver.renderTemplate(data.getSystemPrompt(), ctx, inputs);
        String userPromptTemplate = data.getUserPrompt();
        String userPrompt = userPromptTemplate != null
                ? ParamResolver.renderTemplate(userPromptTemplate, ctx, inputs)
                : buildUserPrompt(inputs);

        List<ChatMessage> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(SystemMessage.from(systemPrompt));
        }
        messages.add(UserMessage.from(userPrompt));

        String text;
        String reasoningContent = null;
        Map<String, Object> usage = null;
        InvokeOutcome outcome = null;
        try {
            outcome = invokeWithRetry(data, messages);
            text = outcome.text;
            if (outcome.tokenUsage != null) {
                usage = new LinkedHashMap<>();
                usage.put("inputTokens", outcome.tokenUsage.inputTokenCount());
                usage.put("outputTokens", outcome.tokenUsage.outputTokenCount());
                usage.put("totalTokens", outcome.tokenUsage.totalTokenCount());
            }
            recordLlmLog(node, data, ctx, systemPrompt, userPrompt, text, outcome.tokenUsage, null);
        } catch (Exception e) {
            recordLlmLog(node, data, ctx, systemPrompt, userPrompt, null, null, e);
            text = handleFailure(node, data, e);
        }

        return new NodeOutput(node.getId(), mapOutputs(text, reasoningContent, usage, data.getOutputParams()));
    }

    private InvokeOutcome invokeWithRetry(LLMNodeData data, List<ChatMessage> messages) throws Exception {
        int maxAttempts = 1 + Math.max(0, data.getRetryCount() == null ? 0 : data.getRetryCount());
        ChatModel model = chatModelProvider.provide(data);
        Exception last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                ChatResponse resp = model.chat(ChatRequest.builder().messages(messages).build());
                return new InvokeOutcome(resp.aiMessage().text(), resp.tokenUsage());
            } catch (Exception e) {
                last = e;
                LOGGER.warn("LLM 调用失败 attempt={}/{} err={}", attempt, maxAttempts, e.getMessage());
            }
        }
        throw last;
    }

    /** 记录一次 LLM 调用日志。recorder 或 modelQueryProvider 缺失时静默跳过。 */
    private void recordLlmLog(Node node, LLMNodeData data, ExecutionContext ctx,
                              String systemPrompt, String userPrompt,
                              String output, TokenUsage tokenUsage, Exception error) {
        if (llmLogRecorder == null) {
            return;
        }
        try {
            LlmModelDTO modelDTO = null;
            if (modelQueryProvider != null && data.getModelId() != null) {
                try {
                    modelDTO = modelQueryProvider.getModel(data.getModelId());
                } catch (Exception e) {
                    LOGGER.debug("查询 LLM 模型信息失败,日志将不带模型详情", e);
                }
            }
            LlmModelConfigDTO config = new LlmModelConfigDTO();
            config.setTemperature(data.getTemperature());

            List<com.iusofts.agentplus.llm.dto.ChatMessage> logMessages = new ArrayList<>();
            if (systemPrompt != null && !systemPrompt.isBlank()) {
                logMessages.add(com.iusofts.agentplus.llm.dto.ChatMessage.builder()
                        .role("system").content(systemPrompt).build());
            }
            logMessages.add(com.iusofts.agentplus.llm.dto.ChatMessage.builder()
                    .role("user").content(userPrompt).build());

            LlmLogRecorder.LlmCallRecorder recorder = llmLogRecorder.recordLlmCall()
                    .traceId(ctx.getRunId())
                    .fromFlow(ctx.getFlowId(), node.getId())
                    .model(modelDTO)
                    .config(config)
                    .inputMessages(logMessages)
                    .operator(ctx.getOperatorId(), ctx.getOrgId());

            if (error != null) {
                recorder.error(null, error.getMessage());
            } else {
                Integer inputTokens = tokenUsage != null ? tokenUsage.inputTokenCount() : null;
                Integer outputTokens = tokenUsage != null ? tokenUsage.outputTokenCount() : null;
                recorder.output(output, inputTokens, outputTokens).success();
            }
            recorder.record();
        } catch (Exception e) {
            LOGGER.warn("写 LLM 日志失败", e);
        }
    }

    private String handleFailure(Node node, LLMNodeData data, Exception e) {
        String strategy = data.getErrorHandling() == null ? "throw" : data.getErrorHandling();
        return switch (strategy.toLowerCase()) {
            case "custom" -> data.getCustomErrorContent() == null ? "" : data.getCustomErrorContent();
            case "continue" -> "";
            default -> throw new WorkflowExecutionException(node.getId(), "LLM 调用最终失败", e);
        };
    }

    private String buildUserPrompt(Map<String, Object> inputs) {
        if (inputs.isEmpty()) {
            return "";
        }
        if (inputs.size() == 1) {
            Object v = inputs.values().iterator().next();
            return v == null ? "" : String.valueOf(v);
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> e : inputs.entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append('\n');
        }
        return sb.toString();
    }

    private Map<String, Object> mapOutputs(String text, String reasoningContent, Map<String, Object> usage, List<OutputParam> outputParams) {
        Map<String, Object> out = new LinkedHashMap<>();
        // 始终包含前端约定的三个默认输出
        out.put("text", text);
        out.put("reasoningContent", reasoningContent);
        out.put("usage", usage);

        if (outputParams == null || outputParams.isEmpty()) {
            return out;
        }

        // 追加自定义输出参数
        if (outputParams.size() == 1) {
            // 只有一个自定义输出时，也尝试先解析 JSON，如果不是 JSON 则用原始 text
            String customName = outputParams.get(0).getName();
            if (!"text".equals(customName) && !"reasoningContent".equals(customName) && !"usage".equals(customName)) {
                Object value = text;
                try {
                    value = JSON.readValue(text, Object.class);
                } catch (Exception ignore) {
                    // 解析失败，保持原始字符串
                }
                out.put(customName, value);
            }
            return out;
        }

        // 多输出参数: 尝试将模型返回作为 JSON 解析
        try {
            Map<String, Object> parsed = JSON.readValue(text, Map.class);
            for (OutputParam p : outputParams) {
                String name = p.getName();
                // 避免覆盖默认输出
                if (!"text".equals(name) && !"reasoningContent".equals(name) && !"usage".equals(name)) {
                    out.put(name, parsed.get(name));
                }
            }
            return out;
        } catch (Exception ignore) {
            LOGGER.debug("LLM 输出非 JSON,仅默认输出可用");
            return out;
        }
    }

    /** 内部结构:一次成功调用的返回文本 + token 使用量。 */
    private record InvokeOutcome(String text, TokenUsage tokenUsage) {
    }
}
