package com.iusofts.agentplus.engine.dag;

import com.iusofts.agentplus.aiflow.vo.workflow.Edge;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 有向无环图,承载工作流拓扑结构。
 *
 * <p>由 {@link DagBuilder} 构建,内部结构不可变。</p>
 *
 * <p>批处理节点的子图信息保存在 {@link #batchSubgraphs} 中,主调度器据此:
 * (1) 跳过所有 batch-internal 节点,交由 BatchNodeExecutor 驱动;
 * (2) 计算 batch 节点 pending 时排除 loop-back 入边;
 * (3) 释放 batch 下游时过滤掉 body-entry 出边。</p>
 *
 * @author Ivan
 */
@Getter
public class DagGraph {

    private final Map<String, Node> nodes;
    private final Map<String, List<Edge>> outgoing;
    private final Map<String, List<Edge>> incoming;
    private final Set<String> startNodeIds;
    private final Map<String, BatchSubgraph> batchSubgraphs;
    private final Set<String> batchInternalNodes;

    public DagGraph(Map<String, Node> nodes,
                    Map<String, List<Edge>> outgoing,
                    Map<String, List<Edge>> incoming,
                    Set<String> startNodeIds,
                    Map<String, BatchSubgraph> batchSubgraphs,
                    Set<String> batchInternalNodes) {
        this.nodes = Collections.unmodifiableMap(nodes);
        this.outgoing = Collections.unmodifiableMap(outgoing);
        this.incoming = Collections.unmodifiableMap(incoming);
        this.startNodeIds = Collections.unmodifiableSet(startNodeIds);
        this.batchSubgraphs = Collections.unmodifiableMap(batchSubgraphs);
        this.batchInternalNodes = Collections.unmodifiableSet(batchInternalNodes);
    }

    public Node node(String id) {
        return nodes.get(id);
    }

    public List<Edge> outgoingOf(String id) {
        return outgoing.getOrDefault(id, Collections.emptyList());
    }

    public List<Edge> incomingOf(String id) {
        return incoming.getOrDefault(id, Collections.emptyList());
    }

    public BatchSubgraph subgraphOf(String batchId) {
        return batchSubgraphs.get(batchId);
    }

    public boolean isBatchInternal(String nodeId) {
        return batchInternalNodes.contains(nodeId);
    }

    /**
     * 批处理节点的子图描述。
     *
     * @param subNodes      子节点 id 集合(node.parentNode == batchId)
     * @param bodyEntries   batch 节点通往子图起点的出边(sourceHandle = "batch-internal-source")
     * @param loopBacks     子图末节点回流到 batch 的入边(targetHandle = "batch-internal-target")
     * @param entryTargets  bodyEntries 的 target 去重集合,子调度器的起点
     * @param returnSources loopBacks 的 source 去重集合,每轮迭代收集输出的节点
     */
    public record BatchSubgraph(Set<String> subNodes,
                                List<Edge> bodyEntries,
                                List<Edge> loopBacks,
                                Set<String> entryTargets,
                                Set<String> returnSources) {
    }
}
