package com.iusofts.agentplus.engine.executor.impl;

import com.alibaba.fastjson2.JSON;
import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.data.KnowledgeNodeData;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.knowledge.KnowledgeRetriever;
import com.iusofts.agentplus.engine.util.ParamResolver;
import com.iusofts.agentplus.knowledge.dto.KnowledgeRetrieveResult;
import com.iusofts.agentplus.ailog.dto.AiTraceContext;

import java.util.Map;

/**
 * 知识库检索节点执行器。
 *
 * <p>把 inputParams 中的所有输入拼接为查询语句，委托 {@link KnowledgeRetriever} 召回。
 * 结果按第一个 outputParam(默认名 chunks/documents/text)输出，便于下游 LLM 节点直接引用。</p>
 *
 * <p>检索日志（embedding 调用与召回明细）由 {@link KnowledgeRetriever} 实现方在底层统一
 * 落库，本执行器只负责透传 {@link AiTraceContext}，不再手动记日志。</p>
 *
 * @author Ivan
 */
public class KnowledgeNodeExecutor implements NodeExecutor {

    private final KnowledgeRetriever retriever;

    public KnowledgeNodeExecutor(KnowledgeRetriever retriever) {
        this.retriever = retriever;
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

        AiTraceContext embeddingCtx = AiTraceContext.builder()
                .traceId(ctx.getRunId())
                .callSource("FLOW")
                .sourceId(ctx.getFlowId())
                .sourceNodeId(node.getId())
                .operatorId(ctx.getOperatorId())
                .orgId(ctx.getOrgId())
                .build();

        KnowledgeRetrieveResult result = retriever.retrieve(data.getKnowledgeIds(), query, topK, embeddingCtx);

        Map<String, Object> outputs = JSON.parseObject(JSON.toJSONString(result));
        return new NodeOutput(node.getId(), outputs);
    }
}
