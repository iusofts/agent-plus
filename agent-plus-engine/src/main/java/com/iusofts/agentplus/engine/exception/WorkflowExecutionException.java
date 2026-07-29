package com.iusofts.agentplus.engine.exception;

/**
 * 工作流执行异常
 *
 * @author Ivan
 */
public class WorkflowExecutionException extends RuntimeException {

    private final String nodeId;

    public WorkflowExecutionException(String message) {
        super(message);
        this.nodeId = null;
    }

    public WorkflowExecutionException(String message, Throwable cause) {
        super(message, cause);
        this.nodeId = null;
    }

    public WorkflowExecutionException(String nodeId, String message) {
        super("[node=" + nodeId + "] " + message);
        this.nodeId = nodeId;
    }

    public WorkflowExecutionException(String nodeId, String message, Throwable cause) {
        super("[node=" + nodeId + "] " + message, cause);
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }
}
