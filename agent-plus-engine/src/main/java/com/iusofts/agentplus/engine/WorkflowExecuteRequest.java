package com.iusofts.agentplus.engine;

import com.iusofts.agentplus.aiflow.vo.workflow.Workflow;
import com.iusofts.agentplus.aiflow.vo.workflow.config.WorkflowConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * 工作流执行请求参数封装。
 *
 * @author Ivan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowExecuteRequest {

    /**
     * 工作流定义(必填)。
     */
    private Workflow workflow;

    /**
     * 工作流配置(必填)。
     */
    private WorkflowConfig config;

    /**
     * 输入参数(必填)。
     */
    private Map<String, Object> inputs;

    /**
     * 运行ID(可选，不传时自动生成)。
     */
    @Builder.Default
    private String runId = UUID.randomUUID().toString();

    /**
     * 流程ID(可选)。
     */
    private Long flowId;

    /**
     * 操作人ID(可选)。
     */
    private Long operatorId;

    /**
     * 组织ID(可选)。
     */
    private Integer orgId;

    /**
     * 试运行标记(可选，0正式/1试运行)。
     */
    private Integer trialFlag;

    /**
     * 流程名称(可选，用于链路追踪标签)。
     */
    private String flowName;


    /**
     * 快捷构建方法（包含流程名称）。
     */
    public static WorkflowExecuteRequest simple(Workflow workflow, WorkflowConfig config, Map<String, Object> inputs, String flowName) {
        return WorkflowExecuteRequest.builder()
                .workflow(workflow)
                .config(config)
                .inputs(inputs)
                .flowName(flowName)
                .build();
    }
}
