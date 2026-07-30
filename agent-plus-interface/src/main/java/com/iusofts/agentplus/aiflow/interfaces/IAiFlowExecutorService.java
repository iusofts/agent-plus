package com.iusofts.agentplus.aiflow.interfaces;

import com.iusofts.agentplus.aiflow.stream.WorkflowStreamEvent;
import com.iusofts.agentplus.aiflow.vo.FlowExecuteResult;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * AI流程公共执行服务接口。
 *
 * @author Ivan Shen
 */
public interface IAiFlowExecutorService {

    /**
     * 执行流程（使用最新已发布版本）。
     *
     * @param flowId     流程ID
     * @param inputs     输入参数
     * @param operatorId 操作人ID
     * @param orgId      组织ID
     * @param trialFlag  试运行标记 0:正式 1:流程试运行 2:节点试运行
     * @return 执行结果
     */
    FlowExecuteResult executeFlow(Long flowId,
                                  Map<String, Object> inputs,
                                  Long operatorId,
                                  Integer orgId,
                                  int trialFlag);

    /**
     * 执行指定版本（供试运行使用）。
     *
     * @param versionId  版本ID
     * @param flowId     流程ID
     * @param inputs     输入参数
     * @param operatorId 操作人ID
     * @param orgId      组织ID
     * @param trialFlag  试运行标记
     * @return 执行结果
     */
    FlowExecuteResult executeVersion(Long versionId,
                                     Long flowId,
                                     Map<String, Object> inputs,
                                     Long operatorId,
                                     Integer orgId,
                                     int trialFlag);

    /**
     * 流式执行流程（使用最新已发布版本）。
     *
     * @param flowId     流程ID
     * @param inputs     输入参数
     * @param operatorId 操作人ID
     * @param orgId      组织ID
     * @param trialFlag  试运行标记 0:正式 1:流程试运行 2:节点试运行
     * @return 事件流
     */
    Flux<WorkflowStreamEvent> streamExecuteFlow(Long flowId,
                                                Map<String, Object> inputs,
                                                Long operatorId,
                                                Integer orgId,
                                                int trialFlag);

    /**
     * 流式执行指定版本（供试运行使用）。
     *
     * @param versionId  版本ID
     * @param flowId     流程ID
     * @param inputs     输入参数
     * @param operatorId 操作人ID
     * @param orgId      组织ID
     * @param trialFlag  试运行标记
     * @return 事件流
     */
    Flux<WorkflowStreamEvent> streamExecuteVersion(Long versionId,
                                                   Long flowId,
                                                   Map<String, Object> inputs,
                                                   Long operatorId,
                                                   Integer orgId,
                                                   int trialFlag);

}