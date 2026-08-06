package com.iusofts.agentplus.trace.constants;

/**
 * Trace 常量。
 *
 * @author Ivan
 */
public final class TraceConstant {

    // ==================== Baggage Keys ====================

    /** OTel Baggage 键：调用来源 */
    public static final String KEY_CALL_SOURCE = "ai.call_source";
    /** OTel Baggage 键：来源 ID */
    public static final String KEY_SOURCE_ID = "ai.source_id";
    /** OTel Baggage 键：来源节点 ID */
    public static final String KEY_SOURCE_NODE_ID = "ai.source_node_id";
    /** OTel Baggage 键：操作人 ID */
    public static final String KEY_OPERATOR_ID = "ai.operator_id";
    /** OTel Baggage 键：组织 ID */
    public static final String KEY_ORG_ID = "ai.org_id";

    // ==================== Span Attribute Keys ====================
    /** OTel Span Attribute 键：标签 */
    public static final String ATTR_LABEL = "label";
    /** OTel Span Attribute 键：调用来源 */
    public static final String ATTR_CALL_SOURCE = "ai.call_source";
    /** OTel Span Attribute 键：来源 ID */
    public static final String ATTR_SOURCE_ID = "ai.source_id";
    /** OTel Span Attribute 键：来源节点 ID */
    public static final String ATTR_SOURCE_NODE_ID = "ai.source_node_id";
    /** OTel Span Attribute 键：操作人 ID */
    public static final String ATTR_OPERATOR_ID = "ai.operator_id";
    /** OTel Span Attribute 键：组织 ID */
    public static final String ATTR_ORG_ID = "ai.org_id";
    /** OTel Span Attribute 键：模型提供商 */
    public static final String ATTR_MODEL_PROVIDER = "ai.model_provider";
    /** OTel Span Attribute 键：模型名称 */
    public static final String ATTR_MODELNAME = "ai.model_name";
    /** OTel Span Attribute 键：tokens */
    public static final String ATTR_TOKENS = "ai.tokens";

    
}
