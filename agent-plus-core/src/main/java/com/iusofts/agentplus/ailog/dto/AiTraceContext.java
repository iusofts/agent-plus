package com.iusofts.agentplus.ailog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 调用链路上下文。
 *
 * <p>大模型 / 嵌入模型调用发生在底层（聊天服务、检索器、存储服务等），底层拿不到
 * 用户与链路信息。由调用方（聊天 / 流程 / 知识库索引）构造本对象透传，用于把 LLM
 * 与 embedding 调用统一落库到 {@code ai_llm_call_log}。</p>
 *
 * @author Ivan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiTraceContext {

    /**
     * 链路追踪 ID。
     */
    private String traceId;

    /**
     * 调用来源：AGENT/CHAT/FLOW/API等。
     */
    private String callSource;

    /**
     * 来源 ID：智能体 ID/会话 ID/流程 ID。
     */
    private Long sourceId;

    /**
     * 来源节点 ID：工作流节点 ID。
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
