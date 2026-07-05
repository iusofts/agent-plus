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
 * @author Ivan
 */
@Getter
public class DagGraph {

    private final Map<String, Node> nodes;
    private final Map<String, List<Edge>> outgoing;
    private final Map<String, List<Edge>> incoming;
    private final Set<String> startNodeIds;

    public DagGraph(Map<String, Node> nodes,
                    Map<String, List<Edge>> outgoing,
                    Map<String, List<Edge>> incoming,
                    Set<String> startNodeIds) {
        this.nodes = Collections.unmodifiableMap(nodes);
        this.outgoing = Collections.unmodifiableMap(outgoing);
        this.incoming = Collections.unmodifiableMap(incoming);
        this.startNodeIds = Collections.unmodifiableSet(startNodeIds);
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
}
