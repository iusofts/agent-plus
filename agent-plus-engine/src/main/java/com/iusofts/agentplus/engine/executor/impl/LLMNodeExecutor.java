package com.iusofts.agentplus.engine.executor.impl;

import com.fasterxml.jackson.core.type.TypeReference;
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
import com.iusofts.agentplus.engine.tool.ToolRegistry;
import com.iusofts.agentplus.engine.util.ParamResolver;
import com.iusofts.agentplus.llm.LlmModelQueryProvider;
import com.iusofts.agentplus.llm.dto.ChatMessage;
import com.iusofts.agentplus.llm.dto.ChatResponse;
import com.iusofts.agentplus.llm.dto.LlmModelConfigDTO;
import com.iusofts.agentplus.llm.dto.LlmModelDTO;
import com.iusofts.agentplus.llm.dto.ToolCall;
import com.iusofts.agentplus.llm.dto.ToolDefinition;
import com.iusofts.agentplus.llm.log.LlmLogRecorder;
import com.iusofts.agentplus.tool.dto.ToolDTO;
import com.iusofts.agentplus.tool.dto.ToolExecuteRequest;
import com.iusofts.agentplus.tool.dto.ToolExecuteResult;
import com.iusofts.agentplus.tool.ToolQueryProvider;
import dev.langchain4j.model.output.TokenUsage;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
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
    /** 单轮执行中工具调用的最大迭代次数，防止模型陷入无限调用 */
    private static final int MAX_TOOL_ITERATIONS = 5;

    private final ChatModelProvider chatModelProvider;
    private final LlmLogRecorder llmLogRecorder;
    private final LlmModelQueryProvider modelQueryProvider;
    private final ToolQueryProvider toolQueryProvider;
    private final ToolRegistry toolRegistry;

    public LLMNodeExecutor(ChatModelProvider chatModelProvider) {
        this(chatModelProvider, null, null, null, null);
    }

    public LLMNodeExecutor(ChatModelProvider chatModelProvider,
                           LlmLogRecorder llmLogRecorder,
                           LlmModelQueryProvider modelQueryProvider,
                           ToolQueryProvider toolQueryProvider,
                           ToolRegistry toolRegistry) {
        this.chatModelProvider = chatModelProvider;
        this.llmLogRecorder = llmLogRecorder;
        this.modelQueryProvider = modelQueryProvider;
        this.toolQueryProvider = toolQueryProvider;
        this.toolRegistry = toolRegistry;
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

        // 构建消息列表
        List<ChatMessage> msgList = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            msgList.add(ChatMessage.builder().role("system").content(systemPrompt).build());
        }
        msgList.add(ChatMessage.builder().role("user").content(userPrompt).build());

        // 构建工具定义
        List<ToolDefinition> toolDefinitions = buildToolDefinitions(data);
        Map<String, Long> toolNameToId = buildToolNameToIdMap(data);

        String text = null;
        String reasoningContent = null;
        Map<String, Object> usage = new LinkedHashMap<>();
        int totalInputTokens = 0;
        int totalOutputTokens = 0;

        try {
            // 工具调用循环: 调用 LLM -> 执行工具 -> 回填结果 -> 再次推理
            ChatResponse response = null;
            for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
                response = invokeWithTools(data, msgList, toolDefinitions);

                // 累加 token 消耗
                if (response.getInputTokens() != null) {
                    totalInputTokens += response.getInputTokens();
                }
                if (response.getOutputTokens() != null) {
                    totalOutputTokens += response.getOutputTokens();
                }

                // 记录本次 LLM 调用日志
                recordLlmLogIteration(node, data, ctx, systemPrompt, userPrompt, response, iteration, ctx.getRunId(), msgList);

                // 模型未请求工具调用，得到最终回答，结束循环
                if (CollectionUtils.isEmpty(response.getToolCalls())) {
                    text = response.getContent();
                    break;
                }

                // 将本轮 assistant 的工具调用请求追加到上下文
                msgList.add(ChatMessage.builder()
                        .role("assistant")
                        .content(response.getContent())
                        .toolCalls(response.getToolCalls())
                        .build());

                // 逐个执行工具，并把结果作为 tool 消息回填
                for (ToolCall toolCall : response.getToolCalls()) {
                    executeToolCall(toolCall, toolNameToId, msgList);
                }
            }

            // 汇总 token 使用量
            usage.put("inputTokens", totalInputTokens);
            usage.put("outputTokens", totalOutputTokens);
            usage.put("totalTokens", totalInputTokens + totalOutputTokens);

        } catch (Exception e) {
            recordLlmLog(node, data, ctx, systemPrompt, userPrompt, null, null, e);
            text = handleFailure(node, data, e);
        }

        return new NodeOutput(node.getId(), mapOutputs(text, reasoningContent, usage, data.getOutputParams()));
    }

    private ChatResponse invokeWithTools(LLMNodeData data, List<ChatMessage> messages, List<ToolDefinition> tools) throws Exception {
        // 由内部 AiChatService 统一处理，包括工具调用
        LlmModelConfigDTO config = new LlmModelConfigDTO();
        config.setTemperature(data.getTemperature());
        return chatModelProvider.chat(data.getModelId(), messages, config, tools);
    }

    /**
     * 根据节点绑定的 toolIds 构建下发给模型的工具规格列表（过滤禁用工具）。
     */
    private List<ToolDefinition> buildToolDefinitions(LLMNodeData data) {
        if (toolQueryProvider == null || CollectionUtils.isEmpty(data.getToolIds())) {
            return null;
        }
        List<ToolDefinition> definitions = new ArrayList<>();
        for (Long toolId : data.getToolIds()) {
            if (toolId == null) {
                continue;
            }
            ToolDTO tool = toolQueryProvider.getById(toolId);
            if (tool == null || tool.getStatus() == null || tool.getStatus() != 1) {
                continue;
            }
            definitions.add(ToolDefinition.builder()
                    .name(tool.getName())
                    .description(tool.getDescription())
                    .parameters(tool.getParamsSchema())
                    .build());
        }
        return definitions.isEmpty() ? null : definitions;
    }

    /**
     * 构建工具名称到工具 ID 的映射，用于将模型返回的工具名解析回 toolId 以便执行。
     */
    private Map<String, Long> buildToolNameToIdMap(LLMNodeData data) {
        Map<String, Long> map = new HashMap<>();
        if (toolQueryProvider == null || CollectionUtils.isEmpty(data.getToolIds())) {
            return map;
        }
        for (Long toolId : data.getToolIds()) {
            if (toolId == null) {
                continue;
            }
            ToolDTO tool = toolQueryProvider.getById(toolId);
            if (tool == null || tool.getStatus() == null || tool.getStatus() != 1) {
                continue;
            }
            map.put(tool.getName(), toolId);
        }
        return map;
    }

    /**
     * 执行一次模型请求的工具调用，将结果作为 tool 消息追加到上下文。
     */
    private void executeToolCall(ToolCall toolCall, Map<String, Long> toolNameToId, List<ChatMessage> msgList) {
        Map<String, Object> params = parseArguments(toolCall.getArguments());

        Long toolId = toolNameToId.get(toolCall.getName());
        ToolExecuteResult result;
        if (toolId == null) {
            result = ToolExecuteResult.error("未找到工具: " + toolCall.getName());
        } else {
            result = toolRegistry.execute(ToolExecuteRequest.builder()
                    .toolId(toolId)
                    .params(params)
                    .build());
        }

        // 回填工具执行结果，供模型下一轮推理
        msgList.add(ChatMessage.builder()
                .role("tool")
                .toolCallId(toolCall.getId())
                .name(toolCall.getName())
                .content(serializeToolResult(result))
                .build());
    }

    /**
     * 解析模型返回的工具调用参数（JSON 字符串）为 Map。
     */
    private Map<String, Object> parseArguments(String arguments) {
        if (org.apache.commons.lang3.StringUtils.isBlank(arguments)) {
            return new HashMap<>();
        }
        try {
            return JSON.readValue(arguments, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            LOGGER.warn("解析工具调用参数失败, arguments={}", arguments, e);
            return new HashMap<>();
        }
    }

    /**
     * 将工具执行结果序列化为回填给模型的文本。
     */
    private String serializeToolResult(ToolExecuteResult result) {
        try {
            if (result.isSuccess()) {
                return JSON.writeValueAsString(result.getData());
            }
            return "工具执行失败: " + result.getErrorMessage();
        } catch (Exception e) {
            LOGGER.warn("序列化工具执行结果失败", e);
            return String.valueOf(result.getData());
        }
    }

    /** 记录一次迭代的 LLM 调用日志。 */
    private void recordLlmLogIteration(Node node, LLMNodeData data, ExecutionContext ctx,
                                       String systemPrompt, String userPrompt, ChatResponse response,
                                       int iteration, String traceId, List<ChatMessage> msgList) {
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

            // 记录当前消息列表用于日志
            List<com.iusofts.agentplus.llm.dto.ChatMessage> logMessages = new ArrayList<>(msgList);

            LlmLogRecorder.LlmCallRecorder recorder = llmLogRecorder.recordLlmCall()
                    .traceId(traceId)
                    .fromFlow(ctx.getFlowId(), node.getId())
                    .model(modelDTO)
                    .config(config)
                    .inputMessages(logMessages)
                    .operator(ctx.getOperatorId(), ctx.getOrgId());

            recorder.output(response.getContent(), response.getInputTokens(), response.getOutputTokens()).success();
            recorder.record();
        } catch (Exception e) {
            LOGGER.warn("写 LLM 日志失败", e);
        }
    }

    /** 记录初始 LLM 调用日志（兼容原有逻辑）。recorder 或 modelQueryProvider 缺失时静默跳过。 */
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

}
