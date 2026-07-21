package com.iusofts.agentplus.aiflow.utils;

import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.Workflow;
import com.iusofts.agentplus.aiflow.vo.workflow.data.EndNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import com.iusofts.agentplus.basic.exception.InvalidParamException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 工作流校验工具类
 *
 * <p>基于 Bean Validation 对 {@link Workflow} 及其节点数据做校验,
 * 校验不通过统一抛出 {@link InvalidParamException}。</p>
 *
 * <p>整体工作流校验时,若违规来自某个节点,错误信息会拼接节点 label 便于定位;
 * 单节点校验则不拼接。</p>
 *
 * @author Ivan
 * @since 2026-07-07
 */
public class WorkflowValidator {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    /** 匹配 nodes[N] 前缀,用于从违规路径中提取节点索引。 */
    private static final Pattern NODE_INDEX_PATTERN = Pattern.compile("^nodes\\[(\\d+)\\]");

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

        Set<ConstraintViolation<Workflow>> violations = VALIDATOR.validate(workflow);
        List<Node> nodes = workflow.getNodes();
        List<String> messages = new ArrayList<>(violations.size());
        for (ConstraintViolation<Workflow> v : violations) {
            String message = v.getMessage();
            String label = resolveViolationNodeLabel(v, nodes);
            if (label != null) {
                message = "[" + label + "] " + message;
            }
            messages.add(message);
        }

        // 自定义校验：EndNode answerMode=text 时 outputParams 不能有 name=text
        if (nodes != null) {
            for (Node node : nodes) {
                if (node.getData() instanceof EndNodeData) {
                    EndNodeData endNodeData = (EndNodeData) node.getData();
                    if ("text".equals(endNodeData.getAnswerMode()) && endNodeData.getOutputParams() != null) {
                        for (OutputParam outputParam : endNodeData.getOutputParams()) {
                            if ("text".equals(outputParam.getName())) {
                                String nodeLabel = resolveNodeLabel(node);
                                messages.add("[" + nodeLabel + "] 结束节点回答模式为text时，输出参数中不能包含name为text的字段，该字段已被输出内容占用");
                            }
                        }
                    }
                }
            }
        }

        if (!messages.isEmpty()) {
            throw new InvalidParamException("工作流校验失败: " + String.join("; ", messages));
        }
    }

    /**
     * 根据节点ID单独校验节点数据,错误信息不拼接节点 label。
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

        Set<ConstraintViolation<Node>> violations = VALIDATOR.validate(target);
        if (violations.isEmpty()) {
            return;
        }

        String detail = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        throw new InvalidParamException("节点校验失败: " + detail);
    }

    /**
     * 从违规的 propertyPath 中解析出节点索引,并返回对应节点的 label;
     * 若违规不属于任何节点,返回 null。
     */
    private static String resolveViolationNodeLabel(ConstraintViolation<Workflow> violation, List<Node> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }
        Matcher matcher = NODE_INDEX_PATTERN.matcher(violation.getPropertyPath().toString());
        if (!matcher.find()) {
            return null;
        }
        int index = Integer.parseInt(matcher.group(1));
        if (index < 0 || index >= nodes.size()) {
            return null;
        }
        return resolveNodeLabel(nodes.get(index));
    }

    /** 节点显示名:优先 Node.label,其次 NodeData.label,最后 Node.id。 */
    private static String resolveNodeLabel(Node node) {
        if (node == null) {
            return "未知节点";
        }
        if (node.getLabel() != null && !node.getLabel().isBlank()) {
            return node.getLabel();
        }
        if (node.getData() != null
                && node.getData().getLabel() != null
                && !node.getData().getLabel().isBlank()) {
            return node.getData().getLabel();
        }
        return node.getId() != null ? node.getId() : "未知节点";
    }

}
