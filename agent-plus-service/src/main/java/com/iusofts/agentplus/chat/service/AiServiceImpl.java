package com.iusofts.agentplus.chat.service;

import com.iusofts.agentplus.chat.interfaces.IAiServiceInterface;
import com.iusofts.agentplus.chat.entity.AiAgent;
import com.iusofts.agentplus.chat.entity.AiCallLog;
import com.iusofts.agentplus.chat.entity.AiConversation;
import com.iusofts.agentplus.chat.entity.AiMessage;
import com.iusofts.agentplus.chat.mapper.AiAgentMapper;
import com.iusofts.agentplus.chat.mapper.AiCallLogMapper;
import com.iusofts.agentplus.chat.vo.AiMessageVo;
import com.iusofts.agentplus.chat.vo.AiServiceCallReqVo;
import com.iusofts.agentplus.chat.vo.AiServiceChatReqVo;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.utils.JsonUtils;
import com.iusofts.agentplus.basic.utils.ModelMapperUtil;
import com.iusofts.agentplus.basic.utils.StringUtils;
import com.iusofts.agentplus.id.service.IdService;
import com.iusofts.agentplus.id.service.IdService.UidTypeEnum;
import com.iusofts.agentplus.llm.AiChatService;
import com.iusofts.agentplus.llm.dto.ChatMessage;
import com.iusofts.agentplus.llm.dto.ChatResponse;
import com.iusofts.agentplus.engine.knowledge.KnowledgeRetriever;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    private AiCallLogMapper aiCallLogMapper;

    @Resource
    private AiChatService aiChatService;

    @Resource
    private KnowledgeRetriever knowledgeRetriever;

    /** 知识库召回默认条数 */
    private static final int DEFAULT_RETRIEVAL_TOP_K = 3;

    @Override
    public AiMessageVo chat(AiServiceChatReqVo reqVo) {
        AiMessageVo resultMessage = null;
        AiConversation conversation;
        AiAgent aiAgent = aiAgentMapper.selectById(reqVo.getAgentId());
        List<AiMessageVo> messageVoList = new ArrayList<>();
        if (reqVo.getConversationId() != null) {
            // 如果会话ID不为空 加载历史对话
            conversation = aiConversationService.getById(reqVo.getConversationId());
            if (reqVo.getAgentId() != null) {
                conversation.setAgentId(reqVo.getAgentId());
            } else {
                aiAgent = aiAgentMapper.selectById(conversation.getAgentId());
            }
            messageVoList.addAll(aiMessageService.getList(reqVo.getConversationId()));
        } else {
            // 会话ID为空 创建新对话
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
            conversation.setBusinessId(reqVo.getBusinessID());
            conversation.setAgentId(reqVo.getAgentId());
            conversation.setModelId(aiAgent.getModelId());
            conversation.setCurrentRounds(0);
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

        try {
            // 获取模型ID
            Long modelId = aiAgent.getModelId();
            if (modelId == null) {
                throw new SystemBusinessException("智能体未配置模型");
            }

            // 按智能体配置的上下文轮数裁剪历史，构建对话上下文
            List<ChatMessage> msgList = buildContext(messageVoList, aiAgent);

            // 知识库检索(RAG)：以最新用户问题召回片段，作为上下文注入
            String knowledgeContext = retrieveKnowledge(aiAgent, latestUserQuestion(messageVoList));
            if (StringUtils.isNotBlank(knowledgeContext)) {
                // 插入到系统提示之后、历史对话之前
                int insertIdx = msgList.isEmpty() || !"system".equalsIgnoreCase(msgList.get(0).getRole()) ? 0 : 1;
                msgList.add(insertIdx, ChatMessage.builder().role("system").content(knowledgeContext).build());
            }

            // 智能体生成参数
            Double temperature = aiAgent.getTemperature() == null ? null : aiAgent.getTemperature().doubleValue();
            Integer maxTokens = aiAgent.getMaxReplyLength();

            // 调用 AiChatService
            ChatResponse response = aiChatService.chat(msgList, modelId, temperature, maxTokens);

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

        // 保存上下文
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

        if(CollectionUtils.isNotEmpty(newMessageList)){
            aiMessageService.saveBatch(newMessageList);
        }

        // 轮次+1
        conversation.setCurrentRounds(conversation.getCurrentRounds() + 1);
        conversation.setUpdateTime(LocalDateTime.now());
        conversation.setLastChatTime(LocalDateTime.now());
        aiConversationService.updateById(conversation);
        return resultMessage;
    }

    @Override
    public AiMessageVo call(AiServiceCallReqVo reqVo) {

        log.debug("ai call param:{}", JsonUtils.obj2json(reqVo));

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

        try {
            // 构建对话上下文
            List<ChatMessage> msgList = new ArrayList<>();

            // 预制内容
            for (AiMessageVo msg : messageVoList) {
                msgList.add(ChatMessage.builder().role(msg.getRole()).content(msg.getContent()).build());
            }

            long callTimeStart = System.currentTimeMillis();

            // 获取模型ID
            Long modelId = null;
            Double temperature = null;
            Integer maxTokens = null;
            if (reqVo.getAgentId() != null) {
                AiAgent aiAgent = aiAgentMapper.selectById(reqVo.getAgentId());
                if (aiAgent != null) {
                    modelId = aiAgent.getModelId();
                    temperature = aiAgent.getTemperature() == null ? null : aiAgent.getTemperature().doubleValue();
                    maxTokens = aiAgent.getMaxReplyLength();
                }
            }
            if (modelId == null) {
                throw new SystemBusinessException("智能体未配置模型");
            }

            // 调用 AiChatService
            ChatResponse response = aiChatService.chat(msgList, modelId, temperature, maxTokens);

            long callTimeEnd = System.currentTimeMillis();

            resultMessage = new AiMessageVo();
            resultMessage.setRole("assistant");
            resultMessage.setContent(response.getContent());
            resultMessage.setInputTokens(response.getInputTokens());
            resultMessage.setOutputTokens(response.getOutputTokens());
            resultMessage.setTotalTokens(response.getTotalTokens());
            messageVoList.add(resultMessage);

            // 保存调用日志
            AiCallLog callLog = new AiCallLog();
            callLog.setBusinessType(reqVo.getBusinessType());
            callLog.setBusinessId(reqVo.getBusinessID());
            callLog.setAgentId(reqVo.getAgentId());
            callLog.setInputTokens(response.getInputTokens());
            callLog.setOutputTokens(response.getOutputTokens());
            callLog.setTotalTokens(response.getTotalTokens());
            callLog.setDuration((int) (callTimeEnd-callTimeStart));
            callLog.setTimeSign(LocalDate.now());
            callLog.setCreateBy(reqVo.getOperatorId());
            callLog.setCreateTime(LocalDateTime.now());
            callLog.setOrgId(reqVo.getOrgId());
            aiCallLogMapper.insert(callLog);

        } catch (Exception e) {
            log.error("AI服务异常", e);
        }

        return resultMessage;
    }

    /**
     * 构建发送给模型的上下文消息。
     *
     * <p>system 消息始终保留；user/assistant 历史按智能体配置的上下文轮数保留最近 N 轮
     * (1 轮 = 一问一答, 即最近 N*2 条对话消息)。</p>
     */
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

    /**
     * 取最近一条 user 消息内容，作为知识库检索问句。
     */
    private String latestUserQuestion(List<AiMessageVo> messageVoList) {
        for (int i = messageVoList.size() - 1; i >= 0; i--) {
            AiMessageVo msg = messageVoList.get(i);
            if ("user".equalsIgnoreCase(msg.getRole()) && StringUtils.isNotBlank(msg.getContent())) {
                return msg.getContent();
            }
        }
        return null;
    }

    /**
     * 对智能体绑定的知识库逐个召回片段, 拼成一段可注入对话的上下文文本。
     *
     * @return 拼接后的知识库上下文; 无绑定/无召回时返回 null
     */
    private String retrieveKnowledge(AiAgent aiAgent, String query) {
        if (aiAgent == null || StringUtils.isBlank(query)
                || CollectionUtils.isEmpty(aiAgent.getKnowledgeBaseIds())) {
            return null;
        }
        int topK = aiAgent.getRetrievalTopK() == null || aiAgent.getRetrievalTopK() <= 0
                ? DEFAULT_RETRIEVAL_TOP_K : aiAgent.getRetrievalTopK();

        List<String> chunks = new ArrayList<>();
        for (Long kbId : aiAgent.getKnowledgeBaseIds()) {
            if (kbId == null) {
                continue;
            }
            try {
                chunks.addAll(knowledgeRetriever.retrieve(kbId, query, topK));
            } catch (Exception e) {
                log.error("知识库检索失败: knowledgeBaseId={}", kbId, e);
            }
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
