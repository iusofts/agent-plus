package com.iusofts.agentplus.aiflow.stream;

import com.iusofts.agentplus.aiflow.enums.NodeRunStatusEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * 节点执行流式事件。
 *
 * @author Ivan Shen
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class NodeStreamEvent extends WorkflowStreamEvent {
    /** 节点ID */
    private String nodeId;
    /** 节点类型 */
    private String nodeType;
    /** 节点名称 */
    private String nodeName;
    /** 执行状态 */
    private NodeRunStatusEnum status;
    /** 节点输出（仅 node_complete 事件） */
    private Map<String, Object> output;
    /** 错误信息（仅 node_error 事件） */
    private String errorMessage;

    public static NodeStreamEvent start(String runId, String nodeId, String nodeType, String nodeName) {
        NodeStreamEvent event = new NodeStreamEvent();
        event.setType(WorkflowStreamEventType.NODE_START.value());
        event.setRunId(runId);
        event.setNodeId(nodeId);
        event.setNodeType(nodeType);
        event.setNodeName(nodeName);
        event.setStatus(NodeRunStatusEnum.RUNNING);
        event.setTimestamp(java.time.Instant.now().toEpochMilli());
        return event;
    }

    public static NodeStreamEvent complete(String runId, String nodeId, String nodeType, String nodeName, Map<String, Object> output) {
        NodeStreamEvent event = new NodeStreamEvent();
        event.setType(WorkflowStreamEventType.NODE_COMPLETE.value());
        event.setRunId(runId);
        event.setNodeId(nodeId);
        event.setNodeType(nodeType);
        event.setNodeName(nodeName);
        event.setStatus(NodeRunStatusEnum.SUCCESS);
        event.setOutput(output);
        event.setTimestamp(java.time.Instant.now().toEpochMilli());
        return event;
    }

    public static NodeStreamEvent error(String runId, String nodeId, String nodeType, String nodeName, String errorMessage) {
        NodeStreamEvent event = new NodeStreamEvent();
        event.setType(WorkflowStreamEventType.NODE_ERROR.value());
        event.setRunId(runId);
        event.setNodeId(nodeId);
        event.setNodeType(nodeType);
        event.setNodeName(nodeName);
        event.setStatus(NodeRunStatusEnum.FAILED);
        event.setErrorMessage(errorMessage);
        event.setTimestamp(java.time.Instant.now().toEpochMilli());
        return event;
    }
}