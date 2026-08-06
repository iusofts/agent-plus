package com.iusofts.agentplus.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iusofts.agentplus.aiflow.entity.AiFlow;
import com.iusofts.agentplus.aiflow.enums.PublishingStatusEnum;
import com.iusofts.agentplus.aiflow.constants.FlowGlobalInputConstants;
import com.iusofts.agentplus.aiflow.interfaces.IAiFlowExecutorService;
import com.iusofts.agentplus.aiflow.mapper.AiFlowMapper;
import com.iusofts.agentplus.aiflow.stream.WorkflowStreamEvent;
import com.iusofts.agentplus.aiflow.stream.ConversationInitEvent;
import com.iusofts.agentplus.aiflow.stream.WorkflowCompleteEvent;
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
import com.iusofts.agentplus.trace.TraceUtil;
import com.iusofts.agentplus.trace.annotation.TraceSpan;
import io.opentelemetry.api.trace.SpanKind;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @TraceSpan(name = "chat.stream.agent", label = "发送聊天消息(对话流)", kind = SpanKind.SERVER)
    public AiMessageVo chat(AiServiceChatReqVo reqVo) {
        TraceUtil.setOperator(reqVo.getOperatorId(), reqVo.getOrgId());
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
        // trialFlag: 0正式/1试运行，不传默认为0
        Integer trialFlag = reqVo.getTrialFlag() != null ? reqVo.getTrialFlag() : 0;
        Long versionId = reqVo.getVersionId();
        com.iusofts.agentplus.aiflow.vo.FlowExecuteResult executeResult;
        if (versionId != null) {
            executeResult = aiFlowExecutorService.executeVersion(
                    versionId,
                    chatFlowId,
                    inputs,
                    reqVo.getOperatorId(),
                    reqVo.getOrgId(),
                    trialFlag
            );
        } else {
            executeResult = aiFlowExecutorService.executeFlow(
                    chatFlowId,
                    inputs,
                    reqVo.getOperatorId(),
                    reqVo.getOrgId(),
                    trialFlag
            );
        }

        // 5. 根据 traceId 统计 token 消耗
        String traceId = executeResult.getTraceId();
        int[] tokens = countTokensByTraceId(traceId);
        int inputTokens = tokens[0];
        int outputTokens = tokens[1];
        int totalTokens = tokens[2];

        // 6. 构建返回结果:取 End 节点的 text 作为返回值 resultMessage(给前端展示)
        //    实际的入库逻辑(End + 各 Output 节点)委托给 saveAssistantMessages
        //    无 text 时不兜底,content 留空(让前端知道没有内容)
        Map<String, Object> output = executeResult.getOutput();
        AiMessageVo resultMessage = new AiMessageVo();
        resultMessage.setRole("assistant");
        resultMessage.setAgentId(agentId);
        if (output.containsKey("text")) {
            resultMessage.setContent(String.valueOf(output.get("text")));
        }
        resultMessage.setInputTokens(inputTokens);
        resultMessage.setOutputTokens(outputTokens);
        resultMessage.setTotalTokens(totalTokens);

        // 7. 落库：会话、用户消息、助手回复(End + 各 Output 节点,委托给 saveAssistantMessages)
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
        aiMessageService.saveBatch(newMessageList);

        // 保存助手回复消息(End 节点 + 各 Output 节点)——委托给 saveAssistantMessages
        saveAssistantMessages(conversation, aiAgent, reqVo, traceId, output);

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

    /**
     * 流式对话接口
     */
    @Override
    public Flux<WorkflowStreamEvent> streamChat(AiServiceChatReqVo chatReqVo) {
        // 1. 确定智能体与会话
        Long agentId = chatReqVo.getAgentId();
        boolean isNewConversation = chatReqVo.getConversationId() == null;
        AiConversation conversation = null;
        if (!isNewConversation) {
            conversation = aiConversationService.getById(chatReqVo.getConversationId());
            if (conversation == null) {
                throw new SystemBusinessException("会话不存在");
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

        // 2. 检查对话流发布状态
        AiFlow aiFlow = aiFlowMapper.selectById(chatFlowId);
        if (aiFlow == null) {
            throw new SystemBusinessException("绑定的对话流不存在");
        }
        if (!PublishingStatusEnum.PUBLISHED.getCode().equals(aiFlow.getPublishStatus())) {
            throw new SystemBusinessException("对话流未发布，请先发布后重试");
        }
        String onlineVersion = aiFlow.getOnlineVersion();
        if (StringUtils.isBlank(onlineVersion)) {
            throw new SystemBusinessException("对话流无已发布版本");
        }

        // 3. 构建输入参数
        Map<String, Object> inputs = new HashMap<>();
        inputs.put(FlowGlobalInputConstants.QUERY, chatReqVo.getContent());
        inputs.put(FlowGlobalInputConstants.FILE_LIST, chatReqVo.getFileList());
        inputs.put(FlowGlobalInputConstants.CONVERSATION_ID, chatReqVo.getConversationId());
        inputs.put(FlowGlobalInputConstants.AGENT_ID, agentId);

        // 4. 先创建会话（如果是新对话）
        if (isNewConversation) {
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

        // 5. 先保存用户消息
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

        // 6. 执行流式工作流
        // versionId 优先：试运行指定版本时用 streamExecuteVersion 跑指定版本;
        // 不传时走 streamExecuteFlow 取线上最新已发布版本（与同步 chat() 行为一致）
        Integer trialFlag = chatReqVo.getTrialFlag() != null ? chatReqVo.getTrialFlag() : 0;
        Long versionId = chatReqVo.getVersionId();
        Flux<WorkflowStreamEvent> stream;
        if (versionId != null) {
            stream = aiFlowExecutorService.streamExecuteVersion(
                versionId,
                chatFlowId,
                inputs,
                chatReqVo.getOperatorId(),
                chatReqVo.getOrgId(),
                trialFlag
            );
        } else {
            stream = aiFlowExecutorService.streamExecuteFlow(
                chatFlowId,
                inputs,
                chatReqVo.getOperatorId(),
                chatReqVo.getOrgId(),
                trialFlag
            );
        }

        // 7. 返回事件流。新建会话时,首事件把 conversationId 推回前端;
        //    新建会话场景下,先持久化会话再发首事件,保证前端收到 conversation_init
        //    事件后立即刷新会话列表就能查到该会话(否则会出现"事件已到但接口查不到"的竞态);
        //    workflow_complete 事件到达时入库助手消息(End + 各 Output 节点)
        //    注:workflow_complete 携带的 finalOutput 含 End 节点的 text 和 outputs 数组(每个 Output 一条)
        if (isNewConversation && finalConversation.getId() != null) {
            // 先持久化会话,让前端 conversation_init → 刷新会话列表 的链路能立即看到
            aiConversationService.save(finalConversation);
            String initRunId = String.valueOf(idService.generateUid(UidTypeEnum.CHAT));
            Flux<WorkflowStreamEvent> withInit = Flux.concat(
                Flux.just(ConversationInitEvent.create(initRunId, finalConversation.getId())),
                stream
            );
            return withInit
                    .doOnNext(event -> handleStreamEventForPersist(event, finalConversation, aiAgent, chatReqVo))
                    .doOnComplete(() -> {
                        // 会话已在上面 save,这里只 update 增量字段(rounds/lastChatTime 等)
                        aiConversationService.updateById(finalConversation);
                    });
        }
        return stream
                .doOnNext(event -> handleStreamEventForPersist(event, finalConversation, aiAgent, chatReqVo))
                .doOnComplete(() -> {
                    aiConversationService.updateById(finalConversation);
                });
    }

    /**
     * 流式事件拦截:workflow_complete 事件到达时入库助手消息(End 节点 + 各 Output 节点)。
     * 注:每个工作流执行只会触发一次 workflow_complete,所以入库是幂等的。
     */
    private void handleStreamEventForPersist(WorkflowStreamEvent event,
                                              AiConversation conversation,
                                              AiAgent aiAgent,
                                              AiServiceChatReqVo reqVo) {
        if (!(event instanceof WorkflowCompleteEvent)) {
            return;
        }
        WorkflowCompleteEvent completeEvent = (WorkflowCompleteEvent) event;
        Map<String, Object> output = completeEvent.getOutput();
        if (output == null) {
            return;
        }
        // runId / traceId 用于 token 统计
        String runId = completeEvent.getRunId();
        if (runId == null) {
            runId = completeEvent.getTraceId();
        }
        saveAssistantMessages(conversation, aiAgent, reqVo, runId, output);
    }

    /**
     * 入库助手消息(各 Output 节点先,End 节点后,按执行顺序):
     * - finalOutput.outputs[].text → 各 1 条(Output 节点,按数组顺序 = 执行顺序)
     * - finalOutput.text → 1 条(End 节点,带 tokens)
     * - 既无 text 也无 outputs 时,不兜底,只 log warn(由上层处理)
     *
     * @param runId traceId/runId(用于 token 统计)
     */
    private void saveAssistantMessages(AiConversation conversation,
                                        AiAgent aiAgent,
                                        AiServiceChatReqVo reqVo,
                                        String runId,
                                        Map<String, Object> output) {
        int inputTokens = 0;
        int outputTokens = 0;
        int totalTokens = 0;
        if (runId != null) {
            int[] tokens = countTokensByTraceId(runId);
            inputTokens = tokens[0];
            outputTokens = tokens[1];
            totalTokens = tokens[2];
        }

        List<AiMessageVo> assistantVos = new ArrayList<>();

        // 1. Output 节点的消息(各一条,按 finalOutput.outputs 数组顺序 = 执行顺序)
        if (output.containsKey("outputs")) {
            Object outputsObj = output.get("outputs");
            if (outputsObj instanceof List) {
                List<?> outputs = (List<?>) outputsObj;
                for (Object entry : outputs) {
                    if (entry instanceof Map) {
                        Map<?, ?> entryMap = (Map<?, ?>) entry;
                        Object textObj = entryMap.get("text");
                        if (textObj != null) {
                            AiMessageVo outMsg = new AiMessageVo();
                            outMsg.setRole("assistant");
                            outMsg.setAgentId(aiAgent.getId());
                            outMsg.setContent(String.valueOf(textObj));
                            // Output 节点不携带 tokens(End 节点汇总的 tokens 写在 End 消息上)
                            assistantVos.add(outMsg);
                        }
                    }
                }
            }
        }

        // 2. End 节点的消息(finalOutput.text,带 tokens)—— 排在 Output 之后
        if (output.containsKey("text")) {
            AiMessageVo endMsg = new AiMessageVo();
            endMsg.setRole("assistant");
            endMsg.setAgentId(aiAgent.getId());
            Object textObj = output.get("text");
            endMsg.setContent(textObj != null ? textObj.toString() : "");
            endMsg.setInputTokens(inputTokens);
            endMsg.setOutputTokens(outputTokens);
            endMsg.setTotalTokens(totalTokens);
            assistantVos.add(endMsg);
        }

        // 3. 既无 text 也无 outputs 时,不兜底,只 log warn(由上层处理;正常流程不会出现)
        if (assistantVos.isEmpty()) {
            log.warn("[FlowChatServiceImpl] saveAssistantMessages: finalOutput 既无 text 也无 outputs," +
                    "不执行入库.runId={}, output={}", runId, output);
            return;
        }

        // 4. 逐条入库(顺序保持:Output 在前,End 在后)
        for (AiMessageVo vo : assistantVos) {
            vo.setConversationId(conversation.getId());
            AiMessage aiMessage = ModelMapperUtil.strictMap(vo, AiMessage.class);
            aiMessage.setId(idService.generateUid(UidTypeEnum.CHAT).longValue());
            aiMessage.setConversationId(conversation.getId());
            aiMessage.setAgentId(aiAgent.getId());
            aiMessage.setCreateBy(reqVo.getOperatorId());
            aiMessage.setOrgId(reqVo.getOrgId());
            aiMessageService.save(aiMessage);
        }
    }

}
