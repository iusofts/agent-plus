package com.iusofts.agentplus.engine.executor.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iusofts.agentplus.aiflow.constants.FlowGlobalInputConstants;
import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import com.iusofts.agentplus.aiflow.vo.workflow.data.llm.LLMNodeData;
import com.iusofts.agentplus.ailog.dto.AiTraceContext;
import com.iusofts.agentplus.chat.vo.AiMessageVo;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.exception.WorkflowExecutionException;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.history.HistoryMessageProvider;
import com.iusofts.agentplus.engine.llm.ChatModelProvider;
import com.iusofts.agentplus.engine.tool.ToolRegistry;
import com.iusofts.agentplus.engine.util.ParamResolver;
import com.iusofts.agentplus.llm.dto.AiChatMessage;
import com.iusofts.agentplus.llm.dto.AiChatRequest;
import com.iusofts.agentplus.llm.dto.AiChatResponse;
import com.iusofts.agentplus.llm.dto.LlmModelConfigDTO;
import com.iusofts.agentplus.llm.dto.ToolCall;
import com.iusofts.agentplus.llm.dto.ToolDefinition;
import com.iusofts.agentplus.tool.dto.ToolDTO;
import com.iusofts.agentplus.tool.dto.ToolExecuteRequest;
import com.iusofts.agentplus.tool.dto.ToolExecuteResult;
import com.iusofts.agentplus.tool.ToolQueryProvider;
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
 * <p>每次模型调用后由 {@link ChatModelProvider#chat(AiChatRequest, com.iusofts.agentplus.ailog.dto.AiTraceContext)}
 * 统一写一条 {@code ai_llm_call_log},含消息体与 token 使用量,本执行器不再手动记日志。</p>
 *
 * @author Ivan
 */
public class LLMNodeExecutor implements NodeExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LLMNodeExecutor.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    /** 单轮执行中工具调用的最大迭代次数，防止模型陷入无限调用 */
    private static final int MAX_TOOL_ITERATIONS = 5;

    private final ChatModelProvider chatModelProvider;
    private final ToolQueryProvider toolQueryProvider;
    private final ToolRegistry toolRegistry;
    private final HistoryMessageProvider historyMessageProvider;

    public LLMNodeExecutor(ChatModelProvider chatModelProvider) {
        this(chatModelProvider, null, null, null);
    }

    public LLMNodeExecutor(ChatModelProvider chatModelProvider,
                           ToolQueryProvider toolQueryProvider,
                           ToolRegistry toolRegistry) {
        this(chatModelProvider, toolQueryProvider, toolRegistry, null);
    }

    public LLMNodeExecutor(ChatModelProvider chatModelProvider,
                           ToolQueryProvider toolQueryProvider,
                           ToolRegistry toolRegistry,
                           HistoryMessageProvider historyMessageProvider) {
        this.chatModelProvider = chatModelProvider;
        this.toolQueryProvider = toolQueryProvider;
        this.toolRegistry = toolRegistry;
        this.historyMessageProvider = historyMessageProvider;
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
        List<AiChatMessage> msgList = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            msgList.add(AiChatMessage.builder().role("system").content(systemPrompt).build());
        }

        // 如果开启了会话历史加载并且有 conversationId，则加载历史消息
        loadHistoryMessagesIfEnabled(data, ctx, msgList);

        // 添加当前用户提示词
        msgList.add(AiChatMessage.builder().role("user").content(userPrompt).build());

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
            AiChatResponse response = null;
            for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
                response = invokeWithTools(node, data, ctx, msgList, toolDefinitions);

                // 累加 token 消耗
                if (response.getInputTokens() != null) {
                    totalInputTokens += response.getInputTokens();
                }
                if (response.getOutputTokens() != null) {
                    totalOutputTokens += response.getOutputTokens();
                }

                // 模型未请求工具调用，得到最终回答，结束循环
                if (CollectionUtils.isEmpty(response.getToolCalls())) {
                    text = response.getContent();
                    break;
                }

                // 将本轮 assistant 的工具调用请求追加到上下文
                msgList.add(AiChatMessage.builder()
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
            text = handleFailure(node, data, e);
        }

        return new NodeOutput(node.getId(), mapOutputs(text, reasoningContent, usage, data.getOutputParams()));
    }

    private AiChatResponse invokeWithTools(Node node, LLMNodeData data, ExecutionContext ctx,
                                           List<AiChatMessage> messages, List<ToolDefinition> tools) throws Exception {
        // 由内部 AiChatService 统一处理，包括工具调用；日志由 ChatModelProvider 统一落库
        LlmModelConfigDTO config = new LlmModelConfigDTO();
        config.setTemperature(data.getTemperature());
        AiChatRequest request = AiChatRequest.builder()
                .modelId(data.getModelId())
                .messages(messages)
                .config(config)
                .tools(tools)
                .build();
        AiTraceContext traceContext = AiTraceContext.builder()
                .traceId(ctx.getRunId())
                .callSource("FLOW")
                .sourceId(ctx.getFlowId())
                .sourceNodeId(node.getId())
                .operatorId(ctx.getOperatorId())
                .orgId(ctx.getOrgId())
                .build();
        return chatModelProvider.chat(request, traceContext);
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
     * 如果开启了会话历史加载，则从数据库加载历史消息添加到上下文。
     * 节点配置的携带轮数不能超过智能体设置的上限。
     */
    private void loadHistoryMessagesIfEnabled(LLMNodeData data, ExecutionContext ctx, List<AiChatMessage> msgList) {
        // 检查是否开启历史
        if (data.getEnableHistory() == null || !data.getEnableHistory()) {
            return;
        }
        // 检查是否有 HistoryMessageProvider 实例
        if (historyMessageProvider == null) {
            LOGGER.debug("HistoryMessageProvider not injected, skip loading history messages");
            return;
        }
        // 从全局输入获取 conversationId
        Object convIdObj = ctx.getGlobalInputs().get(FlowGlobalInputConstants.CONVERSATION_ID);
        if (!(convIdObj instanceof Long)) {
            return;
        }
        Long conversationId = (Long) convIdObj;

        // 获取节点配置的上下文轮数
        Integer nodeRounds = data.getContextRounds();
        if (nodeRounds == null || nodeRounds <= 0) {
            return;
        }

        // 从全局输入获取 agentId，如果有智能体配置的上限则应用
        Object agentIdObj = ctx.getGlobalInputs().get(FlowGlobalInputConstants.AGENT_ID);
        if (historyMessageProvider != null && agentIdObj instanceof Long) {
            nodeRounds = historyMessageProvider.clampRoundsByAgentLimit(nodeRounds, (Long) agentIdObj);
        }

        // 硬上限保护，防止加载过多历史
        if (nodeRounds > 10) {
            nodeRounds = 10;
        }
        if (nodeRounds <= 0) {
            return;
        }

        // 只保留最近 N 轮，过滤掉 system 角色
        // 每轮对话包含 user + assistant 两条消息，所以消息数是 rounds * 2
        int keepMessages = nodeRounds * 2;

        // 加载历史消息（按时间升序），直接从数据库查询只返回最后 keepMessages 条
        List<AiMessageVo> history = historyMessageProvider.getHistoryMessages(conversationId, keepMessages);
        if (CollectionUtils.isEmpty(history)) {
            return;
        }

        // 添加到消息列表（system 已经在数据库查询层面过滤掉了）
        for (AiMessageVo msg : history) {
            msgList.add(AiChatMessage.builder()
                    .role(msg.getRole())
                    .content(msg.getContent())
                    .build());
        }
    }

    /**
     * 执行一次模型请求的工具调用，将结果作为 tool 消息追加到上下文。
     */
    private void executeToolCall(ToolCall toolCall, Map<String, Long> toolNameToId, List<AiChatMessage> msgList) {
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
        msgList.add(AiChatMessage.builder()
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
