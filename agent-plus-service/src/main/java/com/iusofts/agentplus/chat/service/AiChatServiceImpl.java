package com.iusofts.agentplus.chat.service;

import com.iusofts.agentplus.chat.interfaces.IAiChatServiceInterface;
import com.iusofts.agentplus.chat.entity.AiAgent;
import com.iusofts.agentplus.chat.entity.AiConversation;
import com.iusofts.agentplus.chat.entity.AiMessage;
import com.iusofts.agentplus.chat.mapper.AiAgentMapper;
import com.iusofts.agentplus.chat.vo.AiMessageVo;
import com.iusofts.agentplus.chat.vo.AiServiceChatReqVo;
import com.iusofts.agentplus.chat.vo.ToolCallTraceVo;
import com.iusofts.agentplus.library.entity.AiKnowledgeBase;
import com.iusofts.agentplus.library.mapper.AiKnowledgeBaseMapper;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.utils.ModelMapperUtil;
import com.iusofts.agentplus.basic.utils.StringUtils;
import com.iusofts.agentplus.id.service.IdService;
import com.iusofts.agentplus.id.service.IdService.UidTypeEnum;
import com.iusofts.agentplus.llm.AiChatService;
import com.iusofts.agentplus.llm.dto.ChatMessage;
import com.iusofts.agentplus.llm.dto.ChatResponse;
import com.iusofts.agentplus.llm.dto.LlmModelConfigDTO;
import com.iusofts.agentplus.llm.dto.ToolCall;
import com.iusofts.agentplus.llm.dto.ToolDefinition;
import com.iusofts.agentplus.llm.LlmModelQueryProvider;
import com.iusofts.agentplus.llm.log.EmbeddingCallContext;
import com.iusofts.agentplus.llm.log.LlmLogRecorder;
import com.iusofts.agentplus.engine.knowledge.KnowledgeRetriever;
import com.iusofts.agentplus.engine.tool.ToolRegistry;
import com.iusofts.agentplus.knowledge.dto.KnowledgeChunk;
import com.iusofts.agentplus.knowledge.dto.KnowledgeRetrieveResult;
import com.iusofts.agentplus.tool.ToolQueryProvider;
import com.iusofts.agentplus.tool.dto.ToolDTO;
import com.iusofts.agentplus.tool.dto.ToolExecuteRequest;
import com.iusofts.agentplus.tool.dto.ToolExecuteResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private AiKnowledgeBaseMapper knowledgeBaseMapper;

    @Resource
    private AiChatService aiChatService;
    @Resource
    private KnowledgeRetriever knowledgeRetriever;
    @Resource
    private LlmModelQueryProvider llmModelQueryProvider;
    @Resource
    private LlmLogRecorder llmLogRecorder;
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
        if (StringUtils.isNotBlank(reqVo.getContent())) {
            AiMessageVo userMsg = new AiMessageVo();
            userMsg.setRole("user");
            userMsg.setContent(reqVo.getContent());
            userMsg.setFileList(reqVo.getFileList());
            requestMsgs.add(userMsg);
        }
        dialog.addAll(requestMsgs);

        String traceId = LlmLogRecorder.generateTraceId();
        String userQuestion = reqVo.getContent();

        // 3. 构建发送给模型的消息：系统提示词与知识库合并为单条 system，动态生成不落库
        String knowledgeContext = retrieveKnowledge(aiAgent, userQuestion, traceId, reqVo.getOperatorId(), reqVo.getOrgId());
        String systemContent = buildSystemPrompt(aiAgent, knowledgeContext);

        List<ChatMessage> msgList = new ArrayList<>();
        if (StringUtils.isNotBlank(systemContent)) {
            msgList.add(ChatMessage.builder().role("system").content(systemContent).build());
        }
        msgList.addAll(buildDialogContext(dialog, aiAgent));

        LlmModelConfigDTO config = buildModelConfig(aiAgent);

        // 4. 构建绑定工具规格，进入"调用 LLM -> 执行工具 -> 回填结果 -> 再次推理"循环
        //    工具由智能体绑定的 toolIds 决定；中间的 tool_calls / tool 结果消息仅在本轮内存循环使用，不落库。
        List<ToolDefinition> toolDefinitions = buildToolDefinitions(aiAgent);
        Map<String, Long> toolNameToId = buildToolNameToIdMap(aiAgent);
        List<ToolCallTraceVo> toolTraces = new ArrayList<>();

        ChatResponse response = null;
        for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
            LocalDateTime llmCallStart = LocalDateTime.now();
            response = aiChatService.chat(msgList, modelId, config, toolDefinitions);

            llmLogRecorder.recordLlmCall()
                .traceId(traceId)
                .startTime(llmCallStart)
                .fromChat(conversation != null ? conversation.getId() : null)
                .model(llmModelQueryProvider.getModel(modelId))
                .config(config)
                .inputMessages(msgList)
                .toolDefinitions(toolDefinitions)
                .output(response.getContent(), response.getInputTokens(), response.getOutputTokens())
                .toolCalls(response.getToolCalls(), response.getFinishReason())
                .success()
                .operator(reqVo.getOperatorId(), reqVo.getOrgId())
                .record();

            // 模型未请求工具调用，得到最终回答，结束循环
            if (CollectionUtils.isEmpty(response.getToolCalls())) {
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
        if (StringUtils.isNotBlank(content)) {
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
        if (aiAgent != null && StringUtils.isNotBlank(aiAgent.getSystemPrompt())) {
            sb.append(aiAgent.getSystemPrompt().trim());
        }
        if (StringUtils.isNotBlank(knowledgeContext)) {
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
    private List<ChatMessage> buildDialogContext(List<AiMessageVo> messageVoList, AiAgent aiAgent) {
        List<AiMessageVo> dialogMsgs = new ArrayList<>();
        for (AiMessageVo msg : messageVoList) {
            if (!"system".equalsIgnoreCase(msg.getRole())) {
                dialogMsgs.add(msg);
            }
        }

        Integer contextRounds = aiAgent == null ? null : aiAgent.getContextRounds();
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

        List<ChatMessage> msgList = new ArrayList<>();
        for (AiMessageVo msg : dialogMsgs) {
            msgList.add(ChatMessage.builder().role(msg.getRole()).content(msg.getContent()).build());
        }
        return msgList;
    }

    private String retrieveKnowledge(AiAgent aiAgent, String query, String traceId, Long operatorId, Integer orgId) {
        if (aiAgent == null || StringUtils.isBlank(query) || CollectionUtils.isEmpty(aiAgent.getKnowledgeBaseIds())) {
            return null;
        }
        int topK = aiAgent.getRetrievalTopK() == null || aiAgent.getRetrievalTopK() <= 0 ? DEFAULT_RETRIEVAL_TOP_K : aiAgent.getRetrievalTopK();

        EmbeddingCallContext embeddingCtx = EmbeddingCallContext.builder()
                .traceId(traceId)
                .operatorId(operatorId)
                .orgId(orgId)
                .build();

        List<String> chunks = new ArrayList<>();
        for (Long kbId : aiAgent.getKnowledgeBaseIds()) {
            if (kbId == null) {
                continue;
            }
            AiKnowledgeBase kb = knowledgeBaseMapper.selectById(kbId);
            String kbName = kb != null ? kb.getName() : null;

            LocalDateTime retrieveStart = LocalDateTime.now();
            KnowledgeRetrieveResult result = knowledgeRetriever.retrieve(kbId, query, topK, embeddingCtx);
            List<String> retrievedChunks = result.getChunks() != null
                    ? result.getChunks().stream().map(KnowledgeChunk::getContent).collect(Collectors.toList())
                    : List.of();
            chunks.addAll(retrievedChunks);

            llmLogRecorder.recordKnowledgeRetrieval()
                .traceId(traceId)
                .startTime(retrieveStart)
                .fromAgent(aiAgent.getId())
                .knowledgeBase(kbId, kbName)
                .query(query)
                .topK(topK)
                .retrievedResult(result)
                .success()
                .operator(operatorId, orgId)
                .record();
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
    private ToolCallTraceVo executeToolCall(ToolCall toolCall, Map<String, Long> toolNameToId, List<ChatMessage> msgList) {
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
        msgList.add(ChatMessage.builder()
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
        if (StringUtils.isBlank(arguments)) {
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
}
