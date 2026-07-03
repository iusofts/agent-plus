package com.iusofts.ai.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.GenerationUsage;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.iusofts.ai.Interfaces.IAiServiceInterface;
import com.iusofts.ai.entity.AiAgent;
import com.iusofts.ai.entity.AiCallLog;
import com.iusofts.ai.entity.AiConversation;
import com.iusofts.ai.entity.AiMessage;
import com.iusofts.ai.enums.AiAgentType;
import com.iusofts.ai.mapper.AiAgentMapper;
import com.iusofts.ai.mapper.AiCallLogMapper;
import com.iusofts.ai.vo.service.AiMessageVo;
import com.iusofts.ai.vo.service.AiServiceCallReqVo;
import com.iusofts.ai.vo.service.AiServiceChatReqVo;
import com.iusofts.basic.utils.JsonUtils;
import com.iusofts.basic.utils.ModelMapperUtil;
import com.iusofts.basic.utils.StringUtils;
import com.iusofts.id.service.IdService;
import com.iusofts.id.service.IdService.UidTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ivan Shen
 */
@Slf4j
@Service
public class AiServiceImpl implements IAiServiceInterface {

    @Value("${dashscope.api-key}")
    private String apiKey;

    @Value("${dashscope.model}")
    private String model;

    // 官方SDK核心对象
    private final Generation gen = new Generation();

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
            conversation.setAgentType(aiAgent.getType());
            conversation.setModel(model);
            conversation.setCurrentRounds(0);
            conversation.setOrgId(reqVo.getOrgId());
            conversation.setCreateBy(reqVo.getOperatorId());
            aiConversationService.save(conversation);

            if (reqVo.isDefaultPrompt() && StringUtils.isNotBlank(aiAgent.getSystemPrompt())) {
                AiMessageVo messageVo = new AiMessageVo();
                messageVo.setRole(Role.SYSTEM.getValue());
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

        boolean needAi = true;
        if (conversation.getCurrentRounds() >= aiAgent.getMaxRounds()) {
            resultMessage = new AiMessageVo();
            resultMessage.setRole(Role.ASSISTANT.getValue());
            resultMessage.setContent(aiAgent.getTransferHuman());
            resultMessage.setNeedTransferHuman(true);
            resultMessage.setConversationId(conversation.getId());
            messageVoList.add(resultMessage);
            needAi = false;
        }

        if (needAi) {
            try {
                // 构建对话上下文
                List<Message> msgList = new ArrayList<>();

                // 预制内容
                for (AiMessageVo msg : messageVoList) {
                    msgList.add(Message.builder().role(msg.getRole()).content(msg.getContent()).build());
                }

                // 调用官方SDK → 1行搞定！
                GenerationParam param = GenerationParam.builder()
                        .apiKey(apiKey)
                        .model(model)
                        .messages(msgList)
                        .resultFormat("message")
                        .build();

                GenerationResult result = gen.call(param);
                String aiReply = result.getOutput().getChoices().get(0).getMessage().getContent();
                GenerationUsage usage = result.getUsage();

                resultMessage = new AiMessageVo();
                resultMessage.setRole(Role.ASSISTANT.getValue());
                resultMessage.setContent(aiReply);
                resultMessage.setInputTokens(usage.getInputTokens());
                resultMessage.setOutputTokens(usage.getOutputTokens());
                resultMessage.setTotalTokens(usage.getTotalTokens());
                resultMessage.setConversationId(conversation.getId());
                messageVoList.add(resultMessage);

            } catch (Exception e) {
                log.error("AI服务异常", e);
            }
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
                aiMessage.setAgentType(aiAgent.getType());
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
            List<Message> msgList = new ArrayList<>();

            // 预制内容
            for (AiMessageVo msg : messageVoList) {
                msgList.add(Message.builder().role(msg.getRole()).content(msg.getContent()).build());
            }

            // 调用官方SDK → 1行搞定！
            GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model(model)
                    .messages(msgList)
                    .resultFormat("message")
                    .build();

            long callTimeStart = System.currentTimeMillis();
            GenerationResult result = gen.call(param);
            String aiReply = result.getOutput().getChoices().get(0).getMessage().getContent();
            GenerationUsage usage = result.getUsage();
            long callTimeEnd = System.currentTimeMillis();

            resultMessage = new AiMessageVo();
            resultMessage.setRole(Role.ASSISTANT.getValue());
            resultMessage.setContent(aiReply);
            resultMessage.setInputTokens(usage.getInputTokens());
            resultMessage.setOutputTokens(usage.getOutputTokens());
            resultMessage.setTotalTokens(usage.getTotalTokens());
            messageVoList.add(resultMessage);
            
            // 保存调用日志
            AiCallLog callLog = new AiCallLog();
            callLog.setBusinessType(reqVo.getBusinessType());
            callLog.setBusinessId(reqVo.getBusinessID());
            callLog.setAgentId(reqVo.getAgentId());
            callLog.setAgentType(reqVo.getAgentType());
            callLog.setInputTokens(usage.getInputTokens());
            callLog.setOutputTokens(usage.getOutputTokens());
            callLog.setTotalTokens(usage.getTotalTokens());
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
