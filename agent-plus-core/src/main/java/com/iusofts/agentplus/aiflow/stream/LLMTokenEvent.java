package com.iusofts.agentplus.aiflow.stream;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * LLM token 输出流式事件。
 *
 * @author Ivan Shen
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class LLMTokenEvent extends WorkflowStreamEvent {
    /** 节点ID */
    private String nodeId;
    /** 节点类型 */
    private String nodeType;
    /** 节点名称 */
    private String nodeName;
    /** 当前输出的 token 内容 */
    private String token;
    /** 已累计的完整内容 */
    private String accumulatedContent;
    /** 是否为最后一个 token */
    private Boolean isLast;

    public static LLMTokenEvent token(String runId, String nodeId, String nodeType, String nodeName,
                                       String token, String accumulatedContent, Boolean isLast) {
        LLMTokenEvent event = new LLMTokenEvent();
        event.setType(WorkflowStreamEventType.LLM_TOKEN.value());
        event.setRunId(runId);
        event.setNodeId(nodeId);
        event.setNodeType(nodeType);
        event.setNodeName(nodeName);
        event.setToken(token);
        event.setAccumulatedContent(accumulatedContent);
        event.setIsLast(isLast);
        event.setTimestamp(java.time.Instant.now().toEpochMilli());
        return event;
    }
}
