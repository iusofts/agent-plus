package com.iusofts.agentplus.ailog.dto;

import com.iusofts.agentplus.trace.TraceUtil;
import com.iusofts.agentplus.trace.constants.TraceConstant;

/**
 * AI 调用链路上下文。
 *
 * <p>已废弃！请使用 OpenTelemetry Baggage 和 {@link TraceUtil} 来传递业务属性。
 *
 * @author Ivan
 * @deprecated 使用 {@link TraceUtil} 替代
 */
@Deprecated
public class AiTraceContext {

    /** @deprecated 已迁移到 {@link TraceConstant#KEY_CALL_SOURCE} */
    @Deprecated
    public static final String ATTR_CALL_SOURCE = TraceConstant.KEY_CALL_SOURCE;
    /** @deprecated 已迁移到 {@link TraceConstant#KEY_SOURCE_ID} */
    @Deprecated
    public static final String ATTR_SOURCE_ID = TraceConstant.KEY_SOURCE_ID;
    /** @deprecated 已迁移到 {@link TraceConstant#KEY_SOURCE_NODE_ID} */
    @Deprecated
    public static final String ATTR_SOURCE_NODE_ID = TraceConstant.KEY_SOURCE_NODE_ID;
    /** @deprecated 已迁移到 {@link TraceConstant#KEY_OPERATOR_ID} */
    @Deprecated
    public static final String ATTR_OPERATOR_ID = TraceConstant.KEY_OPERATOR_ID;
    /** @deprecated 已迁移到 {@link TraceConstant#KEY_ORG_ID} */
    @Deprecated
    public static final String ATTR_ORG_ID = TraceConstant.KEY_ORG_ID;

    /**
     * 链路追踪 ID。
     * <p>优先从当前 OTel Span 获取，与工作流/chat 链路自动对齐。
     */
    private String traceId;

    /**
     * 调用来源：AGENT/CHAT/FLOW/API 等。
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

    // ==================== 兼容方法 - 内部使用 Baggage ====================

    /**
     * 从当前 OpenTelemetry Baggage 创建上下文（traceId 自动获取）。
     * <p>如果没有 active span，traceId 回退为 UUID。
     */
    public static AiTraceContext create() {
        return fromBaggage();
    }

    /**
     * 从当前 OpenTelemetry Baggage 创建上下文，并设置来源信息。
     */
    public static AiTraceContext fromSource(String callSource, Long sourceId) {
        TraceUtil.setCallSource(callSource, sourceId);
        return fromBaggage();
    }

    /**
     * 从当前 OpenTelemetry Baggage 创建上下文，设置完整来源信息。
     */
    public static AiTraceContext fromSource(String callSource, Long sourceId, String sourceNodeId) {
        TraceUtil.setCallSource(callSource, sourceId, sourceNodeId);
        return fromBaggage();
    }

    /**
     * 从当前 OpenTelemetry Baggage 创建上下文，设置操作人信息。
     */
    public static AiTraceContext fromOperator(Long operatorId, Integer orgId) {
        TraceUtil.setOperator(operatorId, orgId);
        return fromBaggage();
    }

    /**
     * 从当前 OpenTelemetry Baggage 创建上下文，设置完整信息。
     */
    public static AiTraceContext of(String callSource, Long sourceId, Long operatorId, Integer orgId) {
        TraceUtil.setAiAttributes(callSource, sourceId, null, operatorId, orgId);
        return fromBaggage();
    }

    /**
     * 从当前 OpenTelemetry Baggage 创建上下文，设置完整信息（含节点）。
     */
    public static AiTraceContext of(String callSource, Long sourceId, String sourceNodeId,
                                     Long operatorId, Integer orgId) {
        TraceUtil.setAiAttributes(callSource, sourceId, sourceNodeId, operatorId, orgId);
        return fromBaggage();
    }

    /**
     * 从当前 Baggage 读取数据构造 AiTraceContext。
     */
    private static AiTraceContext fromBaggage() {
        AiTraceContext ctx = new AiTraceContext();
        ctx.traceId = TraceUtil.currentTraceId();
        ctx.callSource = TraceUtil.getCallSource();
        ctx.sourceId = TraceUtil.getSourceId();
        ctx.sourceNodeId = TraceUtil.getSourceNodeId();
        ctx.operatorId = TraceUtil.getOperatorId();
        ctx.orgId = TraceUtil.getOrgId();
        return ctx;
    }

    /**
     * 将业务属性保存到当前 Baggage。
     */
    public void saveToBaggage() {
        TraceUtil.setAiAttributes(callSource, sourceId, sourceNodeId, operatorId, orgId);
    }

    // ==================== Getters and Setters ====================

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getCallSource() {
        return callSource;
    }

    public void setCallSource(String callSource) {
        this.callSource = callSource;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceNodeId() {
        return sourceNodeId;
    }

    public void setSourceNodeId(String sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }
}
