package com.iusofts.agentplus.aiflow.stream;

/**
 * 工作流流式事件类型。
 */
public enum WorkflowStreamEventType {
    /** 节点开始执行 */
    NODE_START("node_start"),
    /** 节点执行完成 */
    NODE_COMPLETE("node_complete"),
    /** 节点执行失败 */
    NODE_ERROR("node_error"),
    /** 工作流执行完成 */
    WORKFLOW_COMPLETE("workflow_complete"),
    /**
     * 节点产出完整内容事件(LLM / Output / End 节点执行完成后 emit 一次,带节点完整产出文本)。
     * 取代 LLM_TOKEN:不再采用逐 token 增量流式,改为节点完成后一次性推完整内容。
     */
    MESSAGE_COMPLETE("message_complete"),
    /**
     * LLM token 输出流式事件(已弃用:流程不再走逐 token 增量,改用 {@link #MESSAGE_COMPLETE})。
     * 保留枚举值以兼容旧版前端,新代码不应再发送 LLM_TOKEN 事件。
     */
    @Deprecated
    LLM_TOKEN("llm_token"),
    /** 会话初始化(仅在新建会话时由聊天流首事件推送,携带 conversationId) */
    CONVERSATION_INIT("conversation_init");

    private final String value;

    WorkflowStreamEventType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
