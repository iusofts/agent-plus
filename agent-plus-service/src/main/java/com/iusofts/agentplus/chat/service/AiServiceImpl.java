package com.iusofts.agentplus.chat.service;

import com.iusofts.agentplus.chat.interfaces.IAiServiceInterface;
import com.iusofts.agentplus.chat.entity.AiAgent;
import com.iusofts.agentplus.chat.entity.AiConversation;
import com.iusofts.agentplus.chat.entity.AiMessage;
import com.iusofts.agentplus.chat.mapper.AiAgentMapper;
import com.iusofts.agentplus.chat.vo.AiMessageVo;
import com.iusofts.agentplus.chat.vo.AiServiceCallReqVo;
import com.iusofts.agentplus.chat.vo.AiServiceChatReqVo;
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
import com.iusofts.agentplus.llm.LlmModelQueryProvider;
import com.iusofts.agentplus.llm.log.LlmLogRecorder;
import com.iusofts.agentplus.engine.knowledge.KnowledgeRetriever;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 服务实现。
 *
 * @author Ivan Shen
 */
@Slf4j
@Service
public class AiServiceImpl implements IAiServiceInterface {

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

    /** 知识库召回默认条数 */
    private static final int DEFAULT_RETRIEVAL_TOP_K = 3;

    @Override
    public AiMessageVo chat(AiServiceChatReqVo reqVo) {
        AiMessageVo resultMessage = null;
        AiConversation conversation;
        AiAgent aiAgent = aiAgentMapper.selectById(reqVo.getAgentId());
        List<AiMessageVo> messageVoList = new ArrayList<>();
        if (reqVo.getConversationId() != null) {
            conversation = aiConversationService.getById(reqVo.getConversationId());
            if (reqVo.getAgentId() != null) {
                conversation.setAgentId(reqVo.getAgentId());
            } else {
                aiAgent = aiAgentMapper.selectById(conversation.getAgentId());
            }
            messageVoList.addAll(aiMessageService.getList(reqVo.getConversationId()));
        } else {
            conversation = new AiConversation();
            Integer uid = idService.generateUid(UidTypeEnum.CHAT);
            conversation.setId(uid.longValue());
            String title = "新对话";
            if (CollectionUtils.isNotEmpty(reqVo.getMessages()) && StringUtils.isNotBlank(reqVo.getMessages().get(0).getContent())) {
                String firstMessage = reqVo.getMessages().get(0).getContent();
                title = firstMessage.substring(0, Math.min(firstMessage.length(), 15));
            }
            conversation.setTitle(title);
            conversation.setBusinessType(reqVo.getBusinessType());
            conversation.setOrgId(reqVo.getOrgId());
            conversation.setCreateBy(reqVo.getOperatorId());
            aiConversationService.save(conversation);

            if (reqVo.isDefaultPrompt() && StringUtils.isNotBlank(aiAgent.getSystemPrompt())) {
                AiMessageVo messageVo = new AiMessageVo();
                messageVo.setRole("system");
                messageVo.setContent(aiAgent.getSystemPrompt());
                messageVoList.add(messageVo);
            }
        }

        if (CollectionUtils.isNotEmpty(reqVo.getMessages())) {
            reqVo.getMessages().forEach(item -> {
                AiMessageVo messageVo = new AiMessageVo();
                messageVo.setRole(item.getRole());
                messageVo.setContent(item.getContent());
                messageVoList.add(messageVo);
            });
        }

        String traceId = LlmLogRecorder.generateTraceId();
        String userQuestion = latestUserQuestion(messageVoList);

        try {
            Long modelId = aiAgent.getModelId();
            if (modelId == null) {
                throw new SystemBusinessException("智能体未配置模型");
            }

            List<ChatMessage> msgList = buildContext(messageVoList, aiAgent);

            String knowledgeContext = retrieveKnowledge(aiAgent, userQuestion, traceId, reqVo.getOperatorId(), reqVo.getOrgId());
            if (StringUtils.isNotBlank(knowledgeContext)) {
                int insertIdx = msgList.isEmpty() || !"system".equalsIgnoreCase(msgList.get(0).getRole()) ? 0 : 1;
                msgList.add(insertIdx, ChatMessage.builder().role("system").content(knowledgeContext).build());
            }

            LlmModelConfigDTO config = buildModelConfig(aiAgent);

            ChatResponse response = aiChatService.chat(msgList, modelId, config);

            llmLogRecorder.recordLlmCall()
                .traceId(traceId)
                .fromChat(conversation.getId())
                .model(llmModelQueryProvider.getModel(modelId))
                .config(config)
                .inputMessages(msgList)
                .output(response.getContent(), response.getInputTokens(), response.getOutputTokens())
                .success()
                .operator(reqVo.getOperatorId(), reqVo.getOrgId())
                .record();

            resultMessage = new AiMessageVo();
            resultMessage.setRole("assistant");
            resultMessage.setContent(response.getContent());
            resultMessage.setInputTokens(response.getInputTokens());
            resultMessage.setOutputTokens(response.getOutputTokens());
            resultMessage.setTotalTokens(response.getTotalTokens());
            resultMessage.setConversationId(conversation.getId());
            messageVoList.add(resultMessage);

        } catch (Exception e) {
            log.error("AI服务异常", e);
        }

        List<AiMessageVo> newMessageVoList = messageVoList.stream().filter(item -> item.getId() == null).toList();
        List<AiMessage> newMessageList = new ArrayList<>();
        for (AiMessageVo item : newMessageVoList) {
            AiMessage aiMessage = ModelMapperUtil.strictMap(item, AiMessage.class);
            aiMessage.setId(idService.generateUid(UidTypeEnum.CHAT).longValue());
            aiMessage.setConversationId(conversation.getId());
            if (aiAgent != null) {
                aiMessage.setAgentId(aiAgent.getId());
            }
            aiMessage.setCreateBy(reqVo.getOperatorId());
            aiMessage.setOrgId(reqVo.getOrgId());
            newMessageList.add(aiMessage);
        }

        if (CollectionUtils.isNotEmpty(newMessageList)) {
            aiMessageService.saveBatch(newMessageList);
        }

        conversation.setCurrentRounds(conversation.getCurrentRounds() + 1);
        conversation.setUpdateTime(LocalDateTime.now());
        conversation.setLastChatTime(LocalDateTime.now());
        aiConversationService.updateById(conversation);
        return resultMessage;
    }

    @Override
    public AiMessageVo call(AiServiceCallReqVo reqVo) {
        log.debug("ai call param: {}", reqVo);

        AiMessageVo resultMessage = null;
        List<AiMessageVo> messageVoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(reqVo.getMessages())) {
            reqVo.getMessages().forEach(item -> {
                AiMessageVo messageVo = new AiMessageVo();
                messageVo.setRole(item.getRole());
                messageVo.setContent(item.getContent());
                messageVoList.add(messageVo);
            });
        }

        String traceId = LlmLogRecorder.generateTraceId();
        AiAgent aiAgent = reqVo.getAgentId() != null ? aiAgentMapper.selectById(reqVo.getAgentId()) : null;

        try {
            List<ChatMessage> msgList = new ArrayList<>();
            for (AiMessageVo msg : messageVoList) {
                msgList.add(ChatMessage.builder().role(msg.getRole()).content(msg.getContent()).build());
            }

            Long modelId = null;
            LlmModelConfigDTO config = null;
            if (reqVo.getAgentId() != null) {
                if (aiAgent != null) {
                    modelId = aiAgent.getModelId();
                    config = buildModelConfig(aiAgent);
                }
            }
            if (modelId == null) {
                throw new SystemBusinessException("智能体未配置模型");
            }

            ChatResponse response = aiChatService.chat(msgList, modelId, config);

            llmLogRecorder.recordLlmCall()
                .traceId(traceId)
                .fromAgent(reqVo.getAgentId())
                .model(llmModelQueryProvider.getModel(modelId))
                .config(config)
                .inputMessages(msgList)
                .output(response.getContent(), response.getInputTokens(), response.getOutputTokens())
                .success()
                .operator(reqVo.getOperatorId(), reqVo.getOrgId())
                .record();

            resultMessage = new AiMessageVo();
            resultMessage.setRole("assistant");
            resultMessage.setContent(response.getContent());
            resultMessage.setInputTokens(response.getInputTokens());
            resultMessage.setOutputTokens(response.getOutputTokens());
            resultMessage.setTotalTokens(response.getTotalTokens());
            messageVoList.add(resultMessage);

        } catch (Exception e) {
            log.error("AI服务异常", e);
        }

        return resultMessage;
    }

    private LlmModelConfigDTO buildModelConfig(AiAgent aiAgent) {
        LlmModelConfigDTO config = new LlmModelConfigDTO();
        if (aiAgent != null) {
            config.setTemperature(aiAgent.getTemperature() != null ? aiAgent.getTemperature().doubleValue() : null);
            config.setMaxTokens(aiAgent.getMaxReplyLength());
        }
        return config;
    }

    private List<ChatMessage> buildContext(List<AiMessageVo> messageVoList, AiAgent aiAgent) {
        List<AiMessageVo> systemMsgs = new ArrayList<>();
        List<AiMessageVo> dialogMsgs = new ArrayList<>();
        for (AiMessageVo msg : messageVoList) {
            if ("system".equalsIgnoreCase(msg.getRole())) {
                systemMsgs.add(msg);
            } else {
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

        List<ChatMessage> msgList = new ArrayList<>();
        for (AiMessageVo msg : systemMsgs) {
            msgList.add(ChatMessage.builder().role(msg.getRole()).content(msg.getContent()).build());
        }
        for (AiMessageVo msg : dialogMsgs) {
            msgList.add(ChatMessage.builder().role(msg.getRole()).content(msg.getContent()).build());
        }
        return msgList;
    }

    private String latestUserQuestion(List<AiMessageVo> messageVoList) {
        for (int i = messageVoList.size() - 1; i >= 0; i--) {
            AiMessageVo msg = messageVoList.get(i);
            if ("user".equalsIgnoreCase(msg.getRole()) && StringUtils.isNotBlank(msg.getContent())) {
                return msg.getContent();
            }
        }
        return null;
    }

    private String retrieveKnowledge(AiAgent aiAgent, String query, String traceId, Long operatorId, Integer orgId) {
        if (aiAgent == null || StringUtils.isBlank(query) || CollectionUtils.isEmpty(aiAgent.getKnowledgeBaseIds())) {
            return null;
        }
        int topK = aiAgent.getRetrievalTopK() == null || aiAgent.getRetrievalTopK() <= 0 ? DEFAULT_RETRIEVAL_TOP_K : aiAgent.getRetrievalTopK();

        List<String> chunks = new ArrayList<>();
        for (Long kbId : aiAgent.getKnowledgeBaseIds()) {
            if (kbId == null) {
                continue;
            }
            AiKnowledgeBase kb = knowledgeBaseMapper.selectById(kbId);
            String kbName = kb != null ? kb.getName() : null;

            List<String> retrievedChunks = knowledgeRetriever.retrieve(kbId, query, topK);
            chunks.addAll(retrievedChunks);

            llmLogRecorder.recordKnowledgeRetrieval()
                .traceId(traceId)
                .fromAgent(aiAgent.getId())
                .knowledgeBase(kbId, kbName)
                .query(query)
                .topK(topK)
                .retrievedChunks(retrievedChunks, null)
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
}
