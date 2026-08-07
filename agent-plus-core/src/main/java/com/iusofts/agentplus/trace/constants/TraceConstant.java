package com.iusofts.agentplus.trace.constants;

/**
 * Trace 常量。
 *
 * <p>命名规则:Baggage Key 与 Span Attribute Key 统一使用 {@code <域>.<语义>} 的点分层 + 下划线分词形式
 * （如 {@code ai.operator_id}、{@code workflow.run_id}）。相同含义的字段在 Baggage 与 Span Attribute
 * 共用同一个 key 字面量,直接引用同一个常量,避免重复定义。</p>
 *
 * <p>Span Name 统一使用 {@code <域>.<动作>},其中节点/工具等动态名称以 {@code <域>.} 前缀 + 业务 id
 * 拼接（如 {@code node.<nodeId>},{@code tool.<toolName>}）。</p>
 *
 * @author Ivan
 */
public final class TraceConstant {

    private TraceConstant() {
    }
    
    public static final String ROOT_SPAN_ID = "0000000000000000";

    // ==================== Baggage / Span Attribute 共享 Key ====================
    // Baggage 与 Span Attribute 共用同一字面量,这里统一定义一次,Baggage 与属性两端都引用。

    /** 调用来源 */
    public static final String KEY_CALL_SOURCE = "ai.call_source";
    /** 来源 ID */
    public static final String KEY_SOURCE_ID = "ai.source_id";
    /** 来源流程 ID */
    public static final String KEY_SOURCE_FLOW_ID = "ai.source_flow_id";
    /** 来源节点 ID */
    public static final String KEY_SOURCE_NODE_ID = "ai.source_node_id";
    /** 操作人 ID */
    public static final String KEY_OPERATOR_ID = "ai.operator_id";
    /** 组织 ID */
    public static final String KEY_ORG_ID = "ai.org_id";

    // ==================== Span Names ====================

    /** Span 名称:工作流执行 */
    public static final String SPAN_WORKFLOW_EXECUTE = "workflow.execute";
    /** Span 名称:工作流流式执行 */
    public static final String SPAN_WORKFLOW_STREAM_EXECUTE = "workflow.streamExecute";
    /** Span 名称:聊天流式对话 */
    public static final String SPAN_CHAT_STREAM = "chat.stream";
    /** Span 名称:试运行单节点 */
    public static final String SPAN_FLOW_TRIAL_RUN_NODE = "flowTrial.runNode";
    /** Span 名称前缀:节点执行,实际使用 {@code SPAN_NODE_EXECUTE_PREFIX + nodeId} */
    public static final String SPAN_NODE_EXECUTE_PREFIX = "node.";
    /** Span 名称前缀:工具执行,实际使用 {@code SPAN_TOOL_EXECUTE_PREFIX + toolName} */
    public static final String SPAN_TOOL_EXECUTE_PREFIX = "tool.";

    // ==================== Span Attribute Keys ====================

    // --- 通用 ---

    /** Span 标签（人类可读,用于在 Trace UI 中快速识别该 span） */
    public static final String ATTR_LABEL = "label";

    // --- AI 业务(ai.*) ---

    /** 模型提供商 */
    public static final String ATTR_MODEL_PROVIDER = "ai.model_provider";
    /** 模型名称 */
    public static final String ATTR_MODEL_NAME = "ai.model_name";
    /** 消耗 tokens */
    public static final String ATTR_TOKENS = "ai.tokens";
    /** 工具 ID */
    public static final String ATTR_TOOL_ID = "ai.tool_id";
    /** 智能体 ID */
    public static final String ATTR_AGENT_ID = "ai.agent_id";
    /** 会话 ID */
    public static final String ATTR_CONVERSATION_ID = "ai.conversation_id";

    // --- 工作流(workflow.*) ---

    /** 工作流运行 ID（取 OTel traceId） */
    public static final String ATTR_WORKFLOW_RUN_ID = "workflow.run_id";
    /** 工作流定义 ID */
    public static final String ATTR_WORKFLOW_ID = "workflow.id";
    /** 试运行标记 */
    public static final String ATTR_TRIAL_FLAG = "workflow.trial_flag";

    // --- 节点(node.*) ---

    /** 节点 ID */
    public static final String ATTR_NODE_ID = "node.id";
    /** 节点类型 */
    public static final String ATTR_NODE_TYPE = "node.type";
    /** 节点执行状态 */
    public static final String ATTR_NODE_STATUS = "node.status";

    // --- 载荷(ap.payload.*) ---

    /** 入参载荷 JSON 字符串 */
    public static final String ATTR_PAYLOAD_INPUT = "ap.payload.input";
    /** 出参载荷 JSON 字符串 */
    public static final String ATTR_PAYLOAD_OUTPUT = "ap.payload.output";

    // --- 错误(error.*) ---

    /** 异常类型（类名） */
    public static final String ATTR_ERROR_TYPE = "error.type";

    // ==================== Span Attribute Values ====================

    /** 节点执行状态:成功 */
    public static final String NODE_STATUS_SUCCESS = "SUCCESS";

}
