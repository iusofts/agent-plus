package com.iusofts.agentplus.engine.executor.impl;

import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
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

import java.lang.reflect.Array;
import java.util.Collection;
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
    public FlowNodeType type() {
        return FlowNodeType.CONDITION;
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

    private boolean evaluate(ConditionRule r, ExecutionContext ctx) {
        Object left = ParamResolver.resolve(r.getVariable(), ctx);
        String right = r.getValue();
        String op = r.getOperator() == null ? "==" : r.getOperator().toLowerCase();
        return switch (op) {
            case "==", "eq", "equals" -> equalsValue(left, right);
            case "!=", "ne" -> !equalsValue(left, right);
            case ">", "gt" -> compareNumber(left, right) > 0;
            case ">=", "gte" -> compareNumber(left, right) >= 0;
            case "<", "lt" -> compareNumber(left, right) < 0;
            case "<=", "lte" -> compareNumber(left, right) <= 0;
            case "contains" -> contains(left, right);
            case "not_contains", "notcontains", "!contains" -> !contains(left, right);
            case "len_gt" -> length(left) > parseInt(right);
            case "len_gte" -> length(left) >= parseInt(right);
            case "len_lt" -> length(left) < parseInt(right);
            case "len_lte" -> length(left) <= parseInt(right);
            case "is_empty", "empty", "isempty" -> isEmpty(left);
            case "not_empty", "notempty", "isnotempty" -> !isEmpty(left);
            default -> false;
        };
    }

    private boolean equalsValue(Object left, String right) {
        if (left == null) {
            return right == null || right.isEmpty();
        }
        if (left instanceof Boolean b) {
            return b.equals(Boolean.parseBoolean(right));
        }
        if (left instanceof Number n) {
            try {
                return Double.compare(n.doubleValue(), Double.parseDouble(right)) == 0;
            } catch (NumberFormatException ignore) {
                return false;
            }
        }
        return Objects.equals(String.valueOf(left), right);
    }

    private int compareNumber(Object left, String right) {
        try {
            double l = left instanceof Number n ? n.doubleValue() : Double.parseDouble(String.valueOf(left));
            double r = Double.parseDouble(right);
            return Double.compare(l, r);
        } catch (NumberFormatException e) {
            return String.valueOf(left).compareTo(right);
        }
    }

    private boolean contains(Object left, String right) {
        if (left == null || right == null) {
            return false;
        }
        if (left instanceof Collection<?> col) {
            for (Object o : col) {
                if (Objects.equals(String.valueOf(o), right)) {
                    return true;
                }
            }
            return false;
        }
        if (left instanceof Map<?, ?> map) {
            return map.containsKey(right);
        }
        if (left.getClass().isArray()) {
            int n = Array.getLength(left);
            for (int i = 0; i < n; i++) {
                if (Objects.equals(String.valueOf(Array.get(left, i)), right)) {
                    return true;
                }
            }
            return false;
        }
        return String.valueOf(left).contains(right);
    }

    private int length(Object left) {
        if (left == null) {
            return 0;
        }
        if (left instanceof CharSequence cs) {
            return cs.length();
        }
        if (left instanceof Collection<?> col) {
            return col.size();
        }
        if (left instanceof Map<?, ?> map) {
            return map.size();
        }
        if (left.getClass().isArray()) {
            return Array.getLength(left);
        }
        return String.valueOf(left).length();
    }

    private boolean isEmpty(Object left) {
        if (left == null) {
            return true;
        }
        if (left instanceof CharSequence cs) {
            return cs.isEmpty();
        }
        if (left instanceof Collection<?> col) {
            return col.isEmpty();
        }
        if (left instanceof Map<?, ?> map) {
            return map.isEmpty();
        }
        if (left.getClass().isArray()) {
            return Array.getLength(left) == 0;
        }
        return String.valueOf(left).isEmpty();
    }

    private int parseInt(String s) {
        if (s == null) {
            return 0;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
