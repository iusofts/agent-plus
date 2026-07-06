package com.iusofts.agentplus.engine.executor;

import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;

/**
 * 单节点执行器。每种 {@code Node.type} 对应一个实现。
 *
 * @author Ivan
 */
public interface NodeExecutor {

    /** 支持的节点类型,如 {@code Start}、{@code LLM}、{@code Condition} 等。 */
    FlowNodeType type();

    /**
     * 执行节点。实现应保证幂等或至少可重入,以配合重试策略。
     *
     * @param node 节点定义
     * @param ctx  运行时上下文
     * @return 节点输出
     */
    NodeOutput execute(Node node, ExecutionContext ctx) throws Exception;
}
