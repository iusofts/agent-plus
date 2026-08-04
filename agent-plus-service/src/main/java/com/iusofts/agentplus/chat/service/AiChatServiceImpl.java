package com.iusofts.agentplus.chat.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iusofts.agentplus.aiflow.stream.WorkflowStreamEvent;
import com.iusofts.agentplus.aiflow.stream.ConversationInitEvent;
import com.iusofts.agentplus.aiflow.stream.LLMTokenEvent;
import com.iusofts.agentplus.aiflow.stream.WorkflowCompleteEvent;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.utils.ModelMapperUtil;
import com.iusofts.agentplus.basic.utils.StringUtils;
import com.iusofts.agentplus.chat.entity.AiAgent;
import com.iusofts.agentplus.chat.entity.AiConversation;
import com.iusofts.agentplus.chat.entity.AiMessage;
import com.iusofts.agentplus.chat.interfaces.IAiChatServiceInterface;
import com.iusofts.agentplus.chat.mapper.AiAgentMapper;
import com.iusofts.agentplus.chat.vo.AiMessageVo;
import com.iusofts.agentplus.chat.vo.AiServiceChatReqVo;
import com.iusofts.agentplus.chat.vo.ToolCallTraceVo;
import reactor.core.publisher.Flux;
import com.iusofts.agentplus.engine.knowledge.KnowledgeRetriever;
import com.iusofts.agentplus.engine.llm.ChatModelProvider;
import com.iusofts.agentplus.engine.tool.ToolRegistry;
import com.iusofts.agentplus.id.service.IdService;
import com.iusofts.agentplus.id.service.IdService.UidTypeEnum;
import com.iusofts.agentplus.knowledge.dto.KnowledgeChunk;
import com.iusofts.agentplus.knowledge.dto.KnowledgeRetrieveResult;
import com.iusofts.agentplus.llm.dto.*;
import com.iusofts.agentplus.tool.ToolQueryProvider;
import com.iusofts.agentplus.tool.dto.ToolDTO;
import com.iusofts.agentplus.tool.dto.ToolExecuteRequest;
import com.iusofts.agentplus.tool.dto.ToolExecuteResult;
import com.iusofts.agentplus.trace.TraceUtil;
import com.iusofts.agentplus.trace.annotation.TraceSpan;
import io.opentelemetry.api.trace.SpanKind;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 服务实现。
 *
 * <p>方案一：链路信息通过 OpenTelemetry Span Attributes 传递，
 * 不再手动构造和透传 AiTraceContext。
 *
 * @author Ivan Shen
 */
@Slf4j
@Service
public class AiChatServiceImpl implements IAiChatServiceInterface {

    @Resource
    private IdService idService;
    @Resource
    private AiConversationServiceImpl aiConversationService;
    @Resource
    private AiMessageServiceImpl aiMessageService;
    @Resource
    private AiAgentMapper aiAgentMapper;

    @Resource
    private ChatModelProvider chatModelProvider;
    @Resource
    private KnowledgeRetriever knowledgeRetriever;
    @Resource
    private ToolRegistry toolRegistry;
    @Resource
    private ToolQueryProvider toolQueryProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 知识库召回默认条数 */
    private static final int DEFAULT_RETRIEVAL_TOP_K = 3;

    /** 单轮对话中工具调用的最大迭代次数，防止模型陷入无限调用 */
    private static final int MAX_TOOL_ITERATIONS = 5;

    @Override
    @TraceSpan(name = "chat.agent", kind = SpanKind.SERVER)
    public AiMessageVo chat(AiServiceChatReqVo reqVo) {
        // 1. 确定智能体与会话
        Long agentId = reqVo.getAgentId();
        AiConversation conversation = null;
        boolean newConversation = reqVo.getConversationId() == null;
        if (!newConversation) {
            conversation = aiConversationService.getById(reqVo.getConversationId());
            if (conversation == null) {
                throw new SystemBusinessException("会话不存在");
            }
            if (agentId == null) {
                agentId = conversation.getAgentId();
            }
        }

        AiAgent aiAgent = agentId != null ? aiAgentMapper.selectById(agentId) : null;
        if (aiAgent == null) {
            throw new SystemBusinessException("智能体不存在");
        }
        Long modelId = aiAgent.getModelId();
        if (modelId == null) {
            throw new SystemBusinessException("智能体未配置模型");
        }

        // 2. 加载历史对话消息（仅 user/assistant）与本轮用户输入
        List<AiMessageVo> dialog = new ArrayList<>();
        if (!newConversation) {
            dialog.addAll(aiMessageService.getList(reqVo.getConversationId()));
        }
        List<AiMessageVo> requestMsgs = new ArrayList<>();
        if (StringUtils.hasText(reqVo.getContent())) {
            AiMessageVo userMsg = new AiMessageVo();
            userMsg.setRole("user");
            userMsg.setContent(reqVo.getContent());
            userMsg.setFileList(reqVo.getFileList());
            requestMsgs.add(userMsg);
        }
        dialog.addAll(requestMsgs);

        String userQuestion = reqVo.getContent();

        // 3. 构建发送给模型的消息：系统提示词与知识库合并为单条 system，动态生成不落库
        // 设置操作人信息到 Span
        TraceUtil.setOperator(reqVo.getOperatorId(), reqVo.getOrgId());
        String knowledgeContext = retrieveKnowledge(aiAgent, userQuestion, conversation != null ? conversation.getId() : null);
        String systemContent = buildSystemPrompt(aiAgent, knowledgeContext);

        List<AiChatMessage> msgList = new ArrayList<>();
        if (StringUtils.hasText(systemContent)) {
            msgList.add(AiChatMessage.builder().role("system").content(systemContent).build());
        }
        msgList.addAll(buildDialogContext(dialog, aiAgent));

        LlmModelConfigDTO config = buildModelConfig(aiAgent);

        // 4. 构建绑定工具规格，进入"调用 LLM -> 执行工具 -> 回填结果 -> 再次推理"循环
        //    工具由智能体绑定的 toolIds 决定；中间的 tool_calls / tool 结果消息仅在本轮内存循环使用，不落库。
        List<ToolDefinition> toolDefinitions = buildToolDefinitions(aiAgent);
        Map<String, Long> toolNameToId = buildToolNameToIdMap(aiAgent);
        List<ToolCallTraceVo> toolTraces = new ArrayList<>();

        AiChatResponse response = null;
        for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
            // 设置调用来源到 Span（每次循环设置，因为 Span 在当前上下文中）
            TraceUtil.setCallSource("CHAT", conversation != null ? conversation.getId() : null);

            response = chatModelProvider.chat(AiChatRequest.builder()
                .modelId(modelId)
                .messages(msgList)
                .config(config)
                .tools(toolDefinitions)
                .build());

            // 模型未请求工具调用，得到最终回答，结束循环
            if (CollectionUtils.isEmpty(response.getToolCalls())) {
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
                ToolCallTraceVo trace = executeToolCall(toolCall, toolNameToId, msgList);
                toolTraces.add(trace);
            }
        }

        AiMessageVo resultMessage = new AiMessageVo();
        resultMessage.setRole("assistant");
        resultMessage.setContent(response.getContent());
        resultMessage.setInputTokens(response.getInputTokens());
        resultMessage.setOutputTokens(response.getOutputTokens());
        resultMessage.setTotalTokens(response.getTotalTokens());
        if (!toolTraces.isEmpty()) {
            resultMessage.setToolCalls(toolTraces);
        }

        // 5. 调用成功后再落库：会话、新消息、轮次
        if (newConversation) {
            conversation = new AiConversation();
            conversation.setId(idService.generateUid(UidTypeEnum.CHAT).longValue());
            conversation.setAgentId(agentId);
            conversation.setTitle(buildTitle(reqVo.getContent()));
            conversation.setCurrentRounds(0);
            conversation.setOrgId(reqVo.getOrgId());
            conversation.setCreateBy(reqVo.getOperatorId());
        } else {
            conversation.setAgentId(agentId);
        }
        resultMessage.setConversationId(conversation.getId());

        List<AiMessageVo> newMessageVoList = new ArrayList<>(requestMsgs);
        newMessageVoList.add(resultMessage);
        List<AiMessage> newMessageList = new ArrayList<>();
        for (AiMessageVo item : newMessageVoList) {
            AiMessage aiMessage = ModelMapperUtil.strictMap(item, AiMessage.class);
            aiMessage.setId(idService.generateUid(UidTypeEnum.CHAT).longValue());
            aiMessage.setConversationId(conversation.getId());
            aiMessage.setAgentId(aiAgent.getId());
            aiMessage.setCreateBy(reqVo.getOperatorId());
            aiMessage.setOrgId(reqVo.getOrgId());
            newMessageList.add(aiMessage);
        }
        aiMessageService.saveBatch(newMessageList);

        int rounds = conversation.getCurrentRounds() == null ? 0 : conversation.getCurrentRounds();
        conversation.setCurrentRounds(rounds + 1);
        conversation.setUpdateTime(LocalDateTime.now());
        conversation.setLastChatTime(LocalDateTime.now());
        if (newConversation) {
            aiConversationService.save(conversation);
        } else {
            aiConversationService.updateById(conversation);
        }
        return resultMessage;
    }

    private String buildTitle(String content) {
        if (StringUtils.hasText(content)) {
            return content.substring(0, Math.min(content.length(), 15));
        }
        return "新对话";
    }

    private LlmModelConfigDTO buildModelConfig(AiAgent aiAgent) {
        LlmModelConfigDTO config = new LlmModelConfigDTO();
        if (aiAgent != null) {
            config.setTemperature(aiAgent.getTemperature() != null ? aiAgent.getTemperature().doubleValue() : null);
            config.setMaxTokens(aiAgent.getMaxReplyLength());
        }
        return config;
    }

    /**
     * 合并系统提示词与知识库上下文为单条 system 内容。
     * 知识库作为"参考资料"附加在人设提示词之后，避免多条 system 带来的兼容与语义问题。
     */
    private String buildSystemPrompt(AiAgent aiAgent, String knowledgeContext) {
        StringBuilder sb = new StringBuilder();
        if (aiAgent != null && StringUtils.hasText(aiAgent.getSystemPrompt())) {
            sb.append(aiAgent.getSystemPrompt().trim());
        }
        if (StringUtils.hasText(knowledgeContext)) {
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append(knowledgeContext);
        }
        return sb.toString();
    }

    /**
     * 构建对话上下文：仅保留 user/assistant 消息（过滤历史中残留的 system 脏数据），
     * 按配置的上下文轮数裁剪，并保证裁剪后首条为 user。
     */
    private List<AiChatMessage> buildDialogContext(List<AiMessageVo> messageVoList, AiAgent aiAgent) {
        List<AiMessageVo> dialogMsgs = new ArrayList<>();
        for (AiMessageVo msg : messageVoList) {
            if (!"system".equalsIgnoreCase(msg.getRole())) {
                dialogMsgs.add(msg);
            }
        }

        Integer contextRounds = aiAgent != null ? aiAgent.getContextRounds() : null;
        if (contextRounds != null && contextRounds > 0) {
            int keep = contextRounds * 2;
            if (dialogMsgs.size() > keep) {
                dialogMsgs = new ArrayList<>(dialogMsgs.subList(dialogMsgs.size() - keep, dialogMsgs.size()));
            }
        }

        // 保证首条为 user，避免部分模型要求首条消息必须为 user 而报错
        while (!dialogMsgs.isEmpty() && !"user".equalsIgnoreCase(dialogMsgs.get(0).getRole())) {
            dialogMsgs.remove(0);
        }

        List<AiChatMessage> msgList = new ArrayList<>();
        for (AiMessageVo msg : dialogMsgs) {
            msgList.add(AiChatMessage.builder().role(msg.getRole()).content(msg.getContent()).build());
        }
        return msgList;
    }

    private String retrieveKnowledge(AiAgent aiAgent, String query, Long conversationId) {
        if (aiAgent == null || !StringUtils.hasText(query) || CollectionUtils.isEmpty(aiAgent.getKnowledgeBaseIds())) {
            return null;
        }
        int topK = aiAgent.getRetrievalTopK() == null || aiAgent.getRetrievalTopK() <= 0 ? DEFAULT_RETRIEVAL_TOP_K : aiAgent.getRetrievalTopK();

        // 设置调用来源到 Span（知识库检索场景）
        TraceUtil.setCallSource("AGENT", aiAgent.getId());

        List<String> chunks = new ArrayList<>();
        for (Long kbId : aiAgent.getKnowledgeBaseIds()) {
            if (kbId == null) {
                continue;
            }
            // 检索日志（embedding 调用与召回明细）由 KnowledgeRetriever 底层统一落库
            KnowledgeRetrieveResult result = knowledgeRetriever.retrieve(kbId, query, topK);
            List<String> retrievedChunks = result.getChunks() != null
                ? result.getChunks().stream().map(KnowledgeChunk::getContent).collect(Collectors.toList())
                : List.of();
            chunks.addAll(retrievedChunks);
        }
        if (chunks.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder("以下是与用户问题相关的知识库内容，请优先参考作答：\n");
        for (int i = 0; i < chunks.size(); i++) {
            sb.append("[").append(i + 1).append("] ").append(chunks.get(i)).append("\n");
        }
        return sb.toString();
    }

    /**
     * 根据智能体绑定的 toolIds 构建下发给模型的工具规格列表（过滤禁用工具）。
     */
    private List<ToolDefinition> buildToolDefinitions(AiAgent aiAgent) {
        if (aiAgent == null || CollectionUtils.isEmpty(aiAgent.getToolIds())) {
            return null;
        }
        List<ToolDefinition> definitions = new ArrayList<>();
        for (Long toolId : aiAgent.getToolIds()) {
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
    private Map<String, Long> buildToolNameToIdMap(AiAgent aiAgent) {
        Map<String, Long> map = new HashMap<>();
        if (aiAgent == null || CollectionUtils.isEmpty(aiAgent.getToolIds())) {
            return map;
        }
        for (Long toolId : aiAgent.getToolIds()) {
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
     * 执行一次模型请求的工具调用，将结果作为 tool 消息追加到上下文，并返回可展示的调用轨迹。
     */
    private ToolCallTraceVo executeToolCall(ToolCall toolCall, Map<String, Long> toolNameToId, List<AiChatMessage> msgList) {
        ToolCallTraceVo trace = new ToolCallTraceVo();
        trace.setToolName(toolCall.getName());

        Map<String, Object> params = parseArguments(toolCall.getArguments());
        trace.setArguments(params);

        Long toolId = toolNameToId.get(toolCall.getName());
        ToolExecuteResult result;
        if (toolId == null) {
            result = ToolExecuteResult.error("未找到工具: " + toolCall.getName());
        } else {
            trace.setToolId(toolId);
            result = toolRegistry.execute(ToolExecuteRequest.builder()
                .toolId(toolId)
                .params(params)
                .build());
        }

        trace.setSuccess(result.isSuccess());
        trace.setResult(result.getData());
        trace.setErrorMessage(result.getErrorMessage());

        // 回填工具执行结果，供模型下一轮推理
        msgList.add(AiChatMessage.builder()
            .role("tool")
            .toolCallId(toolCall.getId())
            .name(toolCall.getName())
            .content(serializeToolResult(result))
            .build());

        return trace;
    }

    /**
     * 解析模型返回的工具调用参数（JSON 字符串）为 Map。
     */
    private Map<String, Object> parseArguments(String arguments) {
        if (!StringUtils.hasText(arguments)) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(arguments, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("解析工具调用参数失败, arguments={}", arguments, e);
            return new HashMap<>();
        }
    }

    /**
     * 将工具执行结果序列化为回填给模型的文本。
     */
    private String serializeToolResult(ToolExecuteResult result) {
        try {
            if (result.isSuccess()) {
                return objectMapper.writeValueAsString(result.getData());
            }
            return "工具执行失败: " + result.getErrorMessage();
        } catch (Exception e) {
            log.warn("序列化工具执行结果失败", e);
            return String.valueOf(result.getData());
        }
    }

    @Override
    public Flux<WorkflowStreamEvent> streamChat(AiServiceChatReqVo chatReqVo) {
        // 1. 确定智能体与会话
        Long agentId = chatReqVo.getAgentId();
        AiConversation conversation = null;
        boolean newConversation = chatReqVo.getConversationId() == null;
        if (!newConversation) {
            conversation = aiConversationService.getById(chatReqVo.getConversationId());
            if (conversation == null) {
                throw new SystemBusinessException("会话不存在");
            }
            if (agentId == null) {
                agentId = conversation.getAgentId();
            }
        }

        AiAgent aiAgent = agentId != null ? aiAgentMapper.selectById(agentId) : null;
        if (aiAgent == null) {
            throw new SystemBusinessException("智能体不存在");
        }
        Long modelId = aiAgent.getModelId();
        if (modelId == null) {
            throw new SystemBusinessException("智能体未配置模型");
        }

        // 2. 加载历史对话消息
        List<AiMessageVo> dialog = new ArrayList<>();
        if (!newConversation) {
            dialog.addAll(aiMessageService.getList(chatReqVo.getConversationId()));
        }
        List<AiMessageVo> requestMsgs = new ArrayList<>();
        if (StringUtils.hasText(chatReqVo.getContent())) {
            AiMessageVo userMsg = new AiMessageVo();
            userMsg.setRole("user");
            userMsg.setContent(chatReqVo.getContent());
            userMsg.setFileList(chatReqVo.getFileList());
            requestMsgs.add(userMsg);
        }
        dialog.addAll(requestMsgs);

        String userQuestion = chatReqVo.getContent();

        // 3. 构建发送给模型的消息
        TraceUtil.setOperator(chatReqVo.getOperatorId(), chatReqVo.getOrgId());
        String knowledgeContext = retrieveKnowledge(aiAgent, userQuestion, conversation != null ? conversation.getId() : null);
        String systemContent = buildSystemPrompt(aiAgent, knowledgeContext);

        List<AiChatMessage> msgList = new ArrayList<>();
        if (StringUtils.hasText(systemContent)) {
            msgList.add(AiChatMessage.builder().role("system").content(systemContent).build());
        }
        msgList.addAll(buildDialogContext(dialog, aiAgent));

        LlmModelConfigDTO config = buildModelConfig(aiAgent);

        // 4. 构建绑定工具规格
        List<ToolDefinition> toolDefinitions = buildToolDefinitions(aiAgent);

        // 5. 先创建会话（如果是新对话）
        if (newConversation) {
            conversation = new AiConversation();
            conversation.setId(idService.generateUid(UidTypeEnum.CHAT).longValue());
            conversation.setAgentId(agentId);
            conversation.setTitle(buildTitle(chatReqVo.getContent()));
            conversation.setCurrentRounds(0);
            conversation.setOrgId(chatReqVo.getOrgId());
            conversation.setCreateBy(chatReqVo.getOperatorId());
        } else {
            conversation.setAgentId(agentId);
        }
        final AiConversation finalConversation = conversation;

        // 5.1 新建会话场景下立即持久化,保证前端收到 conversation_init 事件后
        //     立即刷新会话列表就能查到该会话(避免"事件已到但接口查不到"的竞态)
        if (newConversation) {
            aiConversationService.save(finalConversation);
        }

        // 6. 先保存用户消息
        if (StringUtils.isNotBlank(chatReqVo.getContent())) {
            AiMessageVo userMsg = new AiMessageVo();
            userMsg.setRole("user");
            userMsg.setContent(chatReqVo.getContent());
            userMsg.setFileList(chatReqVo.getFileList());
            userMsg.setConversationId(conversation.getId());
            userMsg.setAgentId(aiAgent.getId());
            AiMessage userAiMessage = ModelMapperUtil.strictMap(userMsg, AiMessage.class);
            userAiMessage.setId(idService.generateUid(UidTypeEnum.CHAT).longValue());
            userAiMessage.setCreateBy(chatReqVo.getOperatorId());
            userAiMessage.setOrgId(chatReqVo.getOrgId());
            aiMessageService.save(userAiMessage);
        }

        // 7. 生成 runId
        final String runId = String.valueOf(idService.generateUid(UidTypeEnum.CHAT));

        // 创建 final 变量用于 lambda 捕获
        final List<AiChatMessage> finalMsgList = new ArrayList<>(msgList);
        final List<ToolDefinition> finalToolDefinitions = toolDefinitions;
        final Long finalModelId = modelId;
        final LlmModelConfigDTO finalConfig = config;
        final AiAgent finalAiAgent = aiAgent;
        final Long finalConversationId = conversation != null ? conversation.getId() : null;
        final boolean finalNewConversation = newConversation;

        // 8. 创建 Flux 流式响应
        return Flux.create(sink -> {
            try {
                // 新建会话时，首事件把 conversationId 推回前端
                if (finalNewConversation && finalConversationId != null) {
                    sink.next(ConversationInitEvent.create(runId, finalConversationId));
                }

                // 设置调用来源到 Span
                TraceUtil.setCallSource("CHAT", finalConversationId);

                // 如果有工具，暂不支持工具调用的流式（工具调用需要同步执行）
                if (finalToolDefinitions != null && !finalToolDefinitions.isEmpty()) {
                    // 有工具绑定，使用非流式调用，然后一次性返回
                    Map<String, Long> toolNameToId = buildToolNameToIdMap(finalAiAgent);
                    List<ToolCallTraceVo> toolTraces = new ArrayList<>();

                    // 复制一份 msgList 用于工具调用循环
                    List<AiChatMessage> loopMsgList = new ArrayList<>(finalMsgList);

                    AiChatResponse response = null;
                    for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
                        TraceUtil.setCallSource("CHAT", finalConversationId);

                        response = chatModelProvider.chat(AiChatRequest.builder()
                            .modelId(finalModelId)
                            .messages(loopMsgList)
                            .config(finalConfig)
                            .tools(finalToolDefinitions)
                            .build());

                        if (CollectionUtils.isEmpty(response.getToolCalls())) {
                            break;
                        }

                        loopMsgList.add(AiChatMessage.builder()
                            .role("assistant")
                            .content(response.getContent())
                            .toolCalls(response.getToolCalls())
                            .build());

                        for (ToolCall toolCall : response.getToolCalls()) {
                            ToolCallTraceVo trace = executeToolCall(toolCall, toolNameToId, loopMsgList);
                            toolTraces.add(trace);
                        }
                    }

                    // 保存 final 引用
                    final AiChatResponse finalResponse = response;
                    final List<ToolCallTraceVo> finalToolTraces = toolTraces;

                    // 发送完整内容作为一个 LLMTokenEvent (isLast=true)
                    sink.next(LLMTokenEvent.token(runId, "llm", "llm", "LLM",
                        finalResponse.getContent(), finalResponse.getContent(), true));

                    // 构建输出结果
                    Map<String, Object> output = new HashMap<>();
                    output.put("text", finalResponse.getContent());
                    output.put("inputTokens", finalResponse.getInputTokens());
                    output.put("outputTokens", finalResponse.getOutputTokens());
                    output.put("totalTokens", finalResponse.getTotalTokens());
                    if (!finalToolTraces.isEmpty()) {
                        output.put("toolCalls", finalToolTraces);
                    }

                    // 发送完成事件
                    sink.next(WorkflowCompleteEvent.create(runId, output));

                    // 保存助手消息和更新会话
                    saveAssistantMessageAndUpdateConversation(finalResponse.getContent(),
                        finalResponse.getInputTokens(), finalResponse.getOutputTokens(), finalResponse.getTotalTokens(),
                        finalConversation, finalAiAgent, chatReqVo, finalToolTraces, newConversation);

                    sink.complete();
                } else {
                    // 无工具绑定，使用真实流式调用
                    StringBuilder accumulatedContent = new StringBuilder();
                    final int[] totalTokens = {0, 0}; // inputTokens, outputTokens

                    AiChatResponse response = chatModelProvider.streamChat(
                        AiChatRequest.builder()
                            .modelId(finalModelId)
                            .messages(finalMsgList)
                            .config(finalConfig)
                            .tools(null)
                            .build(),
                        token -> {
                            accumulatedContent.append(token);
                            sink.next(LLMTokenEvent.token(runId, "llm", "llm", "LLM",
                                token, accumulatedContent.toString(), false));
                        }
                    );

                    // 保存 final 引用
                    final AiChatResponse finalResponse = response;
                    final String finalAccumulatedContent = accumulatedContent.toString();

                    // 发送最后一个 token 事件
                    sink.next(LLMTokenEvent.token(runId, "llm", "llm", "LLM",
                        "", finalAccumulatedContent, true));

                    // 累加 token
                    if (finalResponse.getInputTokens() != null) totalTokens[0] += finalResponse.getInputTokens();
                    if (finalResponse.getOutputTokens() != null) totalTokens[1] += finalResponse.getOutputTokens();

                    // 构建输出结果
                    Map<String, Object> output = new HashMap<>();
                    output.put("text", finalResponse.getContent());
                    output.put("inputTokens", totalTokens[0]);
                    output.put("outputTokens", totalTokens[1]);
                    output.put("totalTokens", totalTokens[0] + totalTokens[1]);

                    // 发送完成事件
                    sink.next(WorkflowCompleteEvent.create(runId, output));

                    // 保存助手消息和更新会话
                    saveAssistantMessageAndUpdateConversation(finalResponse.getContent(),
                        totalTokens[0], totalTokens[1], totalTokens[0] + totalTokens[1],
                        finalConversation, finalAiAgent, chatReqVo, null, newConversation);

                    sink.complete();
                }
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    /**
     * 保存助手消息和更新会话。
     * <p>新建会话的场景下,会话已在 streamChat 入口处先 save 一次(让前端
     * 收到 conversation_init 后能立即查到),这里只 update 增量字段;
     * 已有会话场景下同样只 update。</p>
     */
    private void saveAssistantMessageAndUpdateConversation(String content, Integer inputTokens, Integer outputTokens, Integer totalTokens,
                                                           AiConversation conversation, AiAgent aiAgent, AiServiceChatReqVo chatReqVo,
                                                           List<ToolCallTraceVo> toolTraces, boolean newConversation) {
        // 构建助手消息
        AiMessageVo resultMessage = new AiMessageVo();
        resultMessage.setRole("assistant");
        resultMessage.setContent(content);
        resultMessage.setInputTokens(inputTokens);
        resultMessage.setOutputTokens(outputTokens);
        resultMessage.setTotalTokens(totalTokens);
        if (toolTraces != null && !toolTraces.isEmpty()) {
            resultMessage.setToolCalls(toolTraces);
        }
        resultMessage.setConversationId(conversation.getId());

        // 保存助手消息
        AiMessage aiMessage = ModelMapperUtil.strictMap(resultMessage, AiMessage.class);
        aiMessage.setId(idService.generateUid(UidTypeEnum.CHAT).longValue());
        aiMessage.setConversationId(conversation.getId());
        aiMessage.setAgentId(aiAgent.getId());
        aiMessage.setCreateBy(chatReqVo.getOperatorId());
        aiMessage.setOrgId(chatReqVo.getOrgId());
        aiMessageService.save(aiMessage);

        // 更新会话(新建会话已先 save,这里只 update 增量字段;newConversation 参数保留以兼容未来扩展)
        int rounds = conversation.getCurrentRounds() == null ? 0 : conversation.getCurrentRounds();
        conversation.setCurrentRounds(rounds + 1);
        conversation.setUpdateTime(LocalDateTime.now());
        conversation.setLastChatTime(LocalDateTime.now());
        aiConversationService.updateById(conversation);
    }
}
