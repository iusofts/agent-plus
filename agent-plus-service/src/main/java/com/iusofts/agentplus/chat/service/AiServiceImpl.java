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
            // 构建对话上下文
            List<ChatMessage> msgList = new ArrayList<>();

            // 预制内容
            for (AiMessageVo msg : messageVoList) {
                msgList.add(ChatMessage.builder().role(msg.getRole()).content(msg.getContent()).build());
            }

            // 获取模型ID
            Long modelId = aiAgent.getModelId();
            if (modelId == null) {
                throw new SystemBusinessException("智能体未配置模型");
            }

            // 调用 AiChatService
            ChatResponse response = aiChatService.chat(msgList, modelId, null);

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
            if (reqVo.getAgentId() != null) {
                AiAgent aiAgent = aiAgentMapper.selectById(reqVo.getAgentId());
                if (aiAgent != null) {
                    modelId = aiAgent.getModelId();
                }
            }
            if (modelId == null) {
                throw new SystemBusinessException("智能体未配置模型");
            }

            // 调用 AiChatService
            ChatResponse response = aiChatService.chat(msgList, modelId, null);

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

}
