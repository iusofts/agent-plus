package com.iusofts.agentplus.engine.executor.impl;

import com.alibaba.fastjson2.JSON;
import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.data.KnowledgeNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.knowledge.KnowledgeRetriever;
import com.iusofts.agentplus.engine.util.ParamResolver;
import com.iusofts.agentplus.knowledge.dto.KnowledgeRetrieveResult;
import com.iusofts.agentplus.llm.log.LlmLogRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 知识库检索节点执行器。
 *
 * <p>把 inputParams 中的所有输入拼接为查询语句，委托 {@link KnowledgeRetriever} 召回。
 * 结果按第一个 outputParam(默认名 chunks/documents/text)输出，便于下游 LLM 节点直接引用。</p>
 *
 * <p>若引擎构造时提供了 {@link LlmLogRecorder},每次检索按传入的知识库逐条记录
 * {@code ai_knowledge_retrieval_log},含召回结果与向量化 token。</p>
 *
 * @author Ivan
 */
public class KnowledgeNodeExecutor implements NodeExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(KnowledgeNodeExecutor.class);

    private final KnowledgeRetriever retriever;
    private final LlmLogRecorder llmLogRecorder;

    public KnowledgeNodeExecutor(KnowledgeRetriever retriever) {
        this(retriever, null);
    }

    public KnowledgeNodeExecutor(KnowledgeRetriever retriever, LlmLogRecorder llmLogRecorder) {
        this.retriever = retriever;
        this.llmLogRecorder = llmLogRecorder;
    }

    @Override
    public FlowNodeType type() {
        return FlowNodeType.KNOWLEDGE;
    }

    @Override
    public NodeOutput execute(Node node, ExecutionContext ctx) {
        KnowledgeNodeData data = (KnowledgeNodeData) node.getData();
        Map<String, Object> inputs = ParamResolver.resolveInputs(data.getInputParams(), ctx);
        String query = inputs.values().stream()
                .filter(v -> v != null)
                .map(String::valueOf)
                .reduce((a, b) -> a + " " + b)
                .orElse("");

        int topK = data.getTopK() == null ? 3 : data.getTopK();
        KnowledgeRetrieveResult result;
        Exception failure = null;
        LocalDateTime retrieveStart = LocalDateTime.now();
        try {
            result = retriever.retrieve(data.getKnowledgeIds(), query, topK);
        } catch (Exception e) {
            failure = e;
            result = null;
            recordRetrievalLog(node, data, ctx, query, topK, null, e, retrieveStart);
            throw e;
        }
        recordRetrievalLog(node, data, ctx, query, topK, result, null, retrieveStart);

        Map<String, Object> outputs = JSON.parseObject(JSON.toJSONString(result));
        return new NodeOutput(node.getId(), outputs);
    }

    /** 按节点上配置的每个知识库记录一条检索日志。 */
    private void recordRetrievalLog(Node node, KnowledgeNodeData data, ExecutionContext ctx,
                                    String query, int topK,
                                    KnowledgeRetrieveResult result, Exception error,
                                    LocalDateTime retrieveStart) {
        if (llmLogRecorder == null) {
            return;
        }
        List<Long> kbIds = data.getKnowledgeIds();
        if (kbIds == null || kbIds.isEmpty()) {
            return;
        }
        List<String> kbNames = data.getKnowledgeNames();
        for (int i = 0; i < kbIds.size(); i++) {
            Long kbId = kbIds.get(i);
            String kbName = kbNames != null && i < kbNames.size() ? kbNames.get(i) : null;
            try {
                LlmLogRecorder.KnowledgeRetrievalRecorder recorder = llmLogRecorder.recordKnowledgeRetrieval()
                        .traceId(ctx.getRunId())
                        .startTime(retrieveStart)
                        .fromFlow(ctx.getFlowId(), node.getId())
                        .knowledgeBase(kbId, kbName)
                        .query(query)
                        .topK(topK)
                        .operator(ctx.getOperatorId(), ctx.getOrgId());
                if (error != null) {
                    recorder.error(error.getMessage());
                } else if (result != null) {
                    // 多知识库合并结果时,单条日志的召回明细复用整体 result
                    recorder.retrievedResult(result).success();
                } else {
                    recorder.success();
                }
                recorder.record();
            } catch (Exception e) {
                LOGGER.warn("写知识库检索日志失败 kbId={}", kbId, e);
            }
        }
    }
}
