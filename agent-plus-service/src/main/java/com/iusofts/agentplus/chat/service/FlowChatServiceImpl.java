package com.iusofts.agentplus.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iusofts.agentplus.aiflow.entity.AiFlow;
import com.iusofts.agentplus.aiflow.enums.PublishingStatusEnum;
import com.iusofts.agentplus.aiflow.constants.FlowGlobalInputConstants;
import com.iusofts.agentplus.aiflow.interfaces.IAiFlowExecutorService;
import com.iusofts.agentplus.aiflow.mapper.AiFlowMapper;
import com.iusofts.agentplus.aiflow.vo.FlowExecuteResult;
import com.iusofts.agentplus.ailog.entity.AiKnowledgeRetrievalLog;
import com.iusofts.agentplus.ailog.entity.AiLlmCallLog;
import com.iusofts.agentplus.ailog.mapper.AiKnowledgeRetrievalLogMapper;
import com.iusofts.agentplus.ailog.mapper.AiLlmCallLogMapper;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.utils.ModelMapperUtil;
import com.iusofts.agentplus.basic.utils.StringUtils;
import com.iusofts.agentplus.chat.entity.AiAgent;
import com.iusofts.agentplus.chat.entity.AiConversation;
import com.iusofts.agentplus.chat.entity.AiMessage;
import com.iusofts.agentplus.chat.interfaces.IAiChatServiceInterface;
import com.iusofts.agentplus.chat.mapper.AiAgentMapper;
import com.iusofts.agentplus.chat.service.AiConversationServiceImpl;
import com.iusofts.agentplus.chat.service.AiMessageServiceImpl;
import com.iusofts.agentplus.chat.vo.AiMessageVo;
import com.iusofts.agentplus.chat.vo.AiServiceChatReqVo;
import com.iusofts.agentplus.id.service.IdService;
import com.iusofts.agentplus.id.service.IdService.UidTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对话流类型智能体 AI 服务实现。
 * 通过绑定的对话流 ID 调用最新发布的流程执行。
 *
 * @author Ivan Shen
 */
@Slf4j
@Service
public class FlowChatServiceImpl implements IAiChatServiceInterface {

    @Resource
    private IdService idService;
    @Resource
    private AiConversationServiceImpl aiConversationService;
    @Resource
    private AiMessageServiceImpl aiMessageService;
    @Resource
    private AiAgentMapper aiAgentMapper;
    @Resource
    private AiFlowMapper aiFlowMapper;
    @Resource
    private IAiFlowExecutorService aiFlowExecutorService;
    @Resource
    private AiLlmCallLogMapper aiLlmCallLogMapper;
    @Resource
    private AiKnowledgeRetrievalLogMapper aiKnowledgeRetrievalLogMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

        com.iusofts.agentplus.chat.entity.AiAgent aiAgent = agentId != null ? aiAgentMapper.selectById(agentId) : null;
        if (aiAgent == null) {
            throw new SystemBusinessException("智能体不存在");
        }

        Long chatFlowId = aiAgent.getChatFlowId();
        if (chatFlowId == null) {
            throw new SystemBusinessException("智能体未绑定对话流");
        }

        // 2. 检查对话流发布状态
        AiFlow aiFlow = aiFlowMapper.selectById(chatFlowId);
        if (aiFlow == null) {
            throw new SystemBusinessException("绑定的对话流不存在");
        }
        if (!PublishingStatusEnum.PUBLISHED.getCode().equals(aiFlow.getPublishStatus())) {
            throw new SystemBusinessException("对话流未发布，请先发布后再使用");
        }
        String onlineVersion = aiFlow.getOnlineVersion();
        if (StringUtils.isBlank(onlineVersion)) {
            throw new SystemBusinessException("对话流无已发布版本");
        }

        // 3. 构建输入参数
        Map<String, Object> inputs = new HashMap<>();
        // 将 query 和 fileList 直接放在全局输入根级，匹配 {{inputs.query}} 的占位符引用方式
        // 与试运行传参方式保持一致
        inputs.put(FlowGlobalInputConstants.QUERY, reqVo.getContent());
        inputs.put(FlowGlobalInputConstants.FILE_LIST, reqVo.getFileList());
        // 传入会话ID和智能体ID，供LLM节点获取历史对话上下文
        inputs.put(FlowGlobalInputConstants.CONVERSATION_ID, reqVo.getConversationId());
        inputs.put(FlowGlobalInputConstants.AGENT_ID, agentId);

        // 4. 调用公共执行服务执行流程（会自动落库 AiFlowRuntime 和 AiFlowRuntimeNode）
        // trialFlag = 0 表示正式运行，不是试运行
        com.iusofts.agentplus.aiflow.vo.FlowExecuteResult executeResult = aiFlowExecutorService.executeFlow(
                chatFlowId,
                inputs,
                reqVo.getOperatorId(),
                reqVo.getOrgId(),
                0
        );

        // 5. 根据 traceId 统计 token 消耗
        String traceId = executeResult.getTraceId();
        int[] tokens = countTokensByTraceId(traceId);
        int inputTokens = tokens[0];
        int outputTokens = tokens[1];
        int totalTokens = tokens[2];

        // 6. 构建返回结果
        Map<String, Object> output = executeResult.getOutput();
        AiMessageVo resultMessage = new AiMessageVo();
        resultMessage.setRole("assistant");
        resultMessage.setAgentId(agentId);

        // 根据 answerMode 获取返回内容：
        // answerMode = "text" 时 EndNode 已输出 "text" 字段，直接取该字段
        // 否则返回整个 output 的 JSON
        if (output.containsKey("text")) {
            Object textObj = output.get("text");
            resultMessage.setContent(textObj != null ? textObj.toString() : "");
        } else {
            // 非 text 模式返回整个 output
            try {
                resultMessage.setContent(objectMapper.writeValueAsString(output));
            } catch (Exception e) {
                log.warn("序列化输出结果失败", e);
                resultMessage.setContent(output.toString());
            }
        }

        resultMessage.setInputTokens(inputTokens);
        resultMessage.setOutputTokens(outputTokens);
        resultMessage.setTotalTokens(totalTokens);

        // 7. 落库：会话、用户消息、助手回复
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

        // 保存用户输入消息
        List<AiMessage> newMessageList = new java.util.ArrayList<>();
        if (StringUtils.isNotBlank(reqVo.getContent())) {
            AiMessageVo userMsg = new AiMessageVo();
            userMsg.setRole("user");
            userMsg.setContent(reqVo.getContent());
            userMsg.setFileList(reqVo.getFileList());
            userMsg.setConversationId(conversation.getId());
            userMsg.setAgentId(aiAgent.getId());
            AiMessage userAiMessage = ModelMapperUtil.strictMap(userMsg, AiMessage.class);
            userAiMessage.setId(idService.generateUid(UidTypeEnum.CHAT).longValue());
            userAiMessage.setCreateBy(reqVo.getOperatorId());
            userAiMessage.setOrgId(reqVo.getOrgId());
            newMessageList.add(userAiMessage);
        }

        // 保存助手回复消息
        AiMessage aiMessage = ModelMapperUtil.strictMap(resultMessage, AiMessage.class);
        aiMessage.setId(idService.generateUid(UidTypeEnum.CHAT).longValue());
        aiMessage.setConversationId(conversation.getId());
        aiMessage.setAgentId(aiAgent.getId());
        aiMessage.setCreateBy(reqVo.getOperatorId());
        aiMessage.setOrgId(reqVo.getOrgId());
        newMessageList.add(aiMessage);

        aiMessageService.saveBatch(newMessageList);

        // 更新会话
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

    /**
     * 根据 traceId (runId) 统计本次执行的 token 消耗
     * 从 ai_llm_call_log 和 ai_knowledge_retrieval_log 汇总
     */
    private int[] countTokensByTraceId(String traceId) {
        int inputTokens = 0;
        int outputTokens = 0;

        // 统计 LLM 调用的 token
        LambdaQueryWrapper<AiLlmCallLog> llmWrapper = Wrappers.lambdaQuery();
        llmWrapper.eq(AiLlmCallLog::getTraceId, traceId);
        List<AiLlmCallLog> llmLogs = aiLlmCallLogMapper.selectList(llmWrapper);
        for (AiLlmCallLog log : llmLogs) {
            if (log.getInputTokens() != null) {
                inputTokens += log.getInputTokens();
            }
            if (log.getOutputTokens() != null) {
                outputTokens += log.getOutputTokens();
            }
        }

        // 统计知识库检索的向量化 token (计入 inputTokens)
        LambdaQueryWrapper<AiKnowledgeRetrievalLog> kbWrapper = Wrappers.lambdaQuery();
        kbWrapper.eq(AiKnowledgeRetrievalLog::getTraceId, traceId);
        List<AiKnowledgeRetrievalLog> kbLogs = aiKnowledgeRetrievalLogMapper.selectList(kbWrapper);
        for (AiKnowledgeRetrievalLog log : kbLogs) {
            if (log.getQueryEmbeddingTokens() != null) {
                inputTokens += log.getQueryEmbeddingTokens();
            }
        }

        int totalTokens = inputTokens + outputTokens;
        return new int[]{inputTokens, outputTokens, totalTokens};
    }
}
