package com.iusofts.agentplus.engine.stream;

import com.iusofts.agentplus.aiflow.stream.WorkflowStreamEvent;

/**
 * 工作流流式事件回调接口。
 *
 * @author Ivan Shen
 */
@FunctionalInterface
public interface WorkflowStreamEventCallback {
    void onEvent(WorkflowStreamEvent event);
}