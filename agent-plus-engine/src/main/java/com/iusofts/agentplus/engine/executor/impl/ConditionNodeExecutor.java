package com.iusofts.agentplus.engine.executor.impl;

import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.data.condition.Condition;
import com.iusofts.agentplus.aiflow.vo.workflow.data.condition.ConditionNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.condition.ConditionRule;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.util.ParamResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 条件节点执行器。
 *
 * <p>按顺序评估 {@code conditions}(每个含若干 rules 与 logic=and/or),
 * 命中的第一个 condition.id 通过 {@link NodeOutput#getChosenBranch()} 上报,
 * 调度器据此激活对应 sourceHandle 的下游边,其他分支被剪枝。</p>
 *
 * <p>没有 condition 命中时返回 {@code chosenBranch="else"},约定 sourceHandle=="else"
 * 的边为兜底分支;若不存在兜底边则该分支的所有下游节点被跳过。</p>
 *
 * @author Ivan
 */
public class ConditionNodeExecutor implements NodeExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionNodeExecutor.class);
    public static final String ELSE_BRANCH = "else";

    @Override
    public String type() {
        return "Condition";
    }

    @Override
    public NodeOutput execute(Node node, ExecutionContext ctx) {
        ConditionNodeData data = (ConditionNodeData) node.getData();
        String chosen = ELSE_BRANCH;
        if (data != null && data.getConditions() != null) {
            for (Condition c : data.getConditions()) {
                if (evaluate(c, ctx)) {
                    chosen = c.getId();
                    break;
                }
            }
        }
        LOGGER.debug("condition node={} chosen={}", node.getId(), chosen);
        Map<String, Object> outputs = new LinkedHashMap<>();
        outputs.put("branch", chosen);
        return new NodeOutput(node.getId(), outputs, chosen);
    }

    private boolean evaluate(Condition c, ExecutionContext ctx) {
        List<ConditionRule> rules = c.getRules();
        if (rules == null || rules.isEmpty()) {
            return false;
        }
        boolean isAnd = c.getLogic() == null || "and".equalsIgnoreCase(c.getLogic());
        boolean result = isAnd;
        for (ConditionRule r : rules) {
            boolean single = evaluate(r, ctx);
            result = isAnd ? (result && single) : (result || single);
        }
        return result;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean evaluate(ConditionRule r, ExecutionContext ctx) {
        Object left = ParamResolver.resolve(r.getVariable(), ctx);
        String right = r.getValue();
        String op = r.getOperator() == null ? "eq" : r.getOperator().toLowerCase();
        return switch (op) {
            case "eq", "==", "equals" -> Objects.equals(String.valueOf(left), right);
            case "ne", "!=" -> !Objects.equals(String.valueOf(left), right);
            case "gt", ">" -> compare(left, right) > 0;
            case "gte", ">=" -> compare(left, right) >= 0;
            case "lt", "<" -> compare(left, right) < 0;
            case "lte", "<=" -> compare(left, right) <= 0;
            case "contains" -> left != null && String.valueOf(left).contains(right);
            case "notcontains", "!contains" -> left == null || !String.valueOf(left).contains(right);
            case "empty", "isempty" -> left == null || String.valueOf(left).isEmpty();
            case "notempty", "isnotempty" -> left != null && !String.valueOf(left).isEmpty();
            case "in" -> right != null && (right.contains("," + left + ",")
                    || right.startsWith(left + ",")
                    || right.endsWith("," + left)
                    || right.equals(String.valueOf(left)));
            default -> false;
        };
    }

    private int compare(Object left, String right) {
        try {
            double l = Double.parseDouble(String.valueOf(left));
            double r = Double.parseDouble(right);
            return Double.compare(l, r);
        } catch (NumberFormatException e) {
            return String.valueOf(left).compareTo(right);
        }
    }
}
