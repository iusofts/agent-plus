package com.iusofts.agentplus.aiflow.stream;

/**
 * 工作流流式事件类型。
 *
 * @author Ivan Shen
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
    /** LLM token 输出（第二阶段） */
    LLM_TOKEN("llm_token"),
    /** 会话初始化（仅在新建会话时由聊天流首事件推送，携带 conversationId） */
    CONVERSATION_INIT("conversation_init");

    private final String value;

    WorkflowStreamEventType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}