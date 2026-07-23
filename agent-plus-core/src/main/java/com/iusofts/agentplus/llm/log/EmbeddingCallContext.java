package com.iusofts.agentplus.llm.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 嵌入模型调用上下文。
 *
 * <p>向量化调用发生在插件底层（检索器 / 存储服务），底层拿不到用户与链路信息。
 * 由调用方（聊天 / 流程 / 知识库索引）构造本对象透传，用于把 embedding 调用
 * 落库到 {@code ai_llm_call_log}。</p>
 *
 * @author Ivan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingCallContext {

    /**
     * 链路追踪 ID。为空时由记录器自动生成。
     */
    private String traceId;

    /**
     * 来源节点 ID（工作流节点场景）。
     */
    private String sourceNodeId;

    /**
     * 操作人 ID。
     */
    private Long operatorId;

    /**
     * 组织 ID。
     */
    private Integer orgId;
}
