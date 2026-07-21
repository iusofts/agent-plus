package com.iusofts.agentplus.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iusofts.agentplus.aiflow.entity.AiFlow;
import com.iusofts.agentplus.aiflow.entity.AiFlowVersion;
import com.iusofts.agentplus.aiflow.enums.PublishingStatusEnum;
import com.iusofts.agentplus.aiflow.mapper.AiFlowMapper;
import com.iusofts.agentplus.aiflow.mapper.AiFlowVersionMapper;
import com.iusofts.agentplus.aiflow.vo.workflow.Workflow;
import com.iusofts.agentplus.aiflow.vo.workflow.config.WorkflowConfig;
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
import com.iusofts.agentplus.engine.WorkflowEngine;
import com.iusofts.agentplus.engine.WorkflowExecutionResult;
import com.iusofts.agentplus.id.service.IdService;
import com.iusofts.agentplus.id.service.IdService.UidTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private AiFlowVersionMapper aiFlowVersionMapper;
    @Resource
    private AiLlmCallLogMapper aiLlmCallLogMapper;
    @Resource
    private AiKnowledgeRetrievalLogMapper aiKnowledgeRetrievalLogMapper;
    @Resource
    private WorkflowEngine workflowEngine;

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

        AiAgent aiAgent = agentId != null ? aiAgentMapper.selectById(agentId) : null;
        if (aiAgent == null) {
            throw new SystemBusinessException("智能体不存在");
        }

        Long chatFlowId = aiAgent.getChatFlowId();
        if (chatFlowId == null) {
            throw new SystemBusinessException("智能体未绑定对话流");
        }

        // 2. 获取对话流最新发布的版本
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

        LambdaQueryWrapper<AiFlowVersion> versionWrapper = Wrappers.lambdaQuery();
        versionWrapper.eq(AiFlowVersion::getFlowId, chatFlowId)
                .eq(AiFlowVersion::getVersionNo, onlineVersion)
                .eq(AiFlowVersion::getPublishingStatus, PublishingStatusEnum.PUBLISHED.getCode());
        AiFlowVersion version = aiFlowVersionMapper.selectOne(versionWrapper);
        if (version == null) {
            throw new SystemBusinessException("对话流发布版本不存在");
        }

        // 3. 反序列化工作流和配置
        Workflow workflow = deserializeWorkflow(version.getFlowJson());
        WorkflowConfig config = deserializeWorkflowConfig(version.getConfigJson());

        // 4. 构建输入参数
        Map<String, Object> inputs = new HashMap<>();
        Map<String, Object> inputParams = new HashMap<>();
        inputParams.put("query", reqVo.getContent());
        inputParams.put("fileList", reqVo.getFileList());
        inputs.put("inputParams", inputParams);

        // 5. 执行工作流
        String runId = newTraceId();
        WorkflowExecutionResult result = workflowEngine.execute(
                workflow,
                config,
                inputs,
                runId,
                chatFlowId,
                reqVo.getOperatorId(),
                reqVo.getOrgId()
        );

        // 6. 统计 token 消耗
        int[] tokens = countTokensByTraceId(runId);
        int inputTokens = tokens[0];
        int outputTokens = tokens[1];
        int totalTokens = tokens[2];

        // 7. 构建返回结果
        Map<String, Object> output = result.getOutput();
        AiMessageVo resultMessage = new AiMessageVo();
        resultMessage.setRole("assistant");
        resultMessage.setAgentId(agentId);

        // 根据 answerMode 获取返回内容
        // answerMode 在 EndNode 中已经处理好了输出：text 模式会输出 "text" 字段
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

        // 8. 落库：会话、用户消息、助手回复
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

    private Workflow deserializeWorkflow(String flowJson) {
        if (flowJson == null || flowJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(flowJson, Workflow.class);
        } catch (Exception e) {
            throw new SystemBusinessException("流程数据解析失败");
        }
    }

    private WorkflowConfig deserializeWorkflowConfig(String configJson) {
        if (configJson == null || configJson.isBlank()) {
            return new WorkflowConfig();
        }
        try {
            return objectMapper.readValue(configJson, WorkflowConfig.class);
        } catch (Exception e) {
            throw new SystemBusinessException("流程配置数据解析失败");
        }
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

    private String newTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
