package com.iusofts.agentplus.aiflow.utils;

import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.Workflow;
import com.iusofts.agentplus.basic.exception.InvalidParamException;
import com.iusofts.agentplus.basic.validation.ValidationUtils;

/**
 * 工作流校验工具类
 *
 * <p>基于 {@link ValidationUtils} 对 {@link Workflow} 及其节点数据做 Bean Validation 校验。
 * 校验不通过统一抛出 {@link InvalidParamException}。</p>
 *
 * @author Ivan
 * @since 2026-07-07
 */
public class WorkflowValidator {

    private WorkflowValidator() {
    }

    /**
     * 校验整份工作流(节点/边/节点数据)。
     *
     * @param workflow 待校验的工作流,不能为空
     */
    public static void validate(Workflow workflow) {
        if (workflow == null) {
            throw new InvalidParamException("工作流数据不能为空");
        }
        ValidationUtils.validate(workflow);
    }

    /**
     * 根据节点ID单独校验节点数据。
     *
     * @param workflow 工作流,不能为空
     * @param nodeId   节点ID,不能为空
     */
    public static void validateNode(Workflow workflow, String nodeId) {
        if (workflow == null) {
            throw new InvalidParamException("工作流数据不能为空");
        }
        if (nodeId == null || nodeId.isBlank()) {
            throw new InvalidParamException("节点ID不能为空");
        }
        if (workflow.getNodes() == null || workflow.getNodes().isEmpty()) {
            throw new InvalidParamException("工作流节点列表不能为空");
        }

        Node target = workflow.getNodes().stream()
                .filter(n -> nodeId.equals(n.getId()))
                .findFirst()
                .orElseThrow(() -> new InvalidParamException("未找到指定节点: " + nodeId));

        ValidationUtils.validate(target);
    }

}
