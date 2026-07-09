package com.iusofts.agentplus.engine.executor.impl;

import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.data.KnowledgeNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.knowledge.KnowledgeRetriever;
import com.iusofts.agentplus.engine.util.ParamResolver;
import com.iusofts.agentplus.knowledge.dto.KnowledgeChunk;
import com.iusofts.agentplus.knowledge.dto.KnowledgeRetrieveResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识库检索节点执行器。
 *
 * <p>把 inputParams 中的所有输入拼接为查询语句,委托 {@link KnowledgeRetriever} 召回。
 * 结果按第一个 outputParam(默认名 chunks/documents/text)输出,便于下游 LLM 节点直接引用。</p>
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
        KnowledgeRetrieveResult result = retriever.retrieve(data.getKnowledgeId(), query, topK);

        // 提取字符串列表保持向后兼容
        List<String> chunkTexts = result.getChunks() != null
                ? result.getChunks().stream().map(KnowledgeChunk::getContent).collect(Collectors.toList())
                : List.of();

        Map<String, Object> outputs = new LinkedHashMap<>();
        String outName = "documents";
        if (data.getOutputParams() != null && !data.getOutputParams().isEmpty()) {
            OutputParam p = data.getOutputParams().get(0);
            if (p.getName() != null) {
                outName = p.getName();
            }
        }
        outputs.put(outName, chunkTexts);
        // 同时输出完整结果对象
        outputs.put("retrieveResult", result);
        return new NodeOutput(node.getId(), outputs);
    }
}
