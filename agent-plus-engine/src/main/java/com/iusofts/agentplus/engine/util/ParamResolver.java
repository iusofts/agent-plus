package com.iusofts.agentplus.engine.util;

import com.iusofts.agentplus.aiflow.vo.workflow.data.common.InputParam;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.ParamMapKey;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 输入参数解析器。
 *
 * <p>支持两种引用:</p>
 * <ul>
 *   <li>结构化: {@link InputParam#getParamMapKey()} 指定 {@code nodeId + name}。</li>
 *   <li>字符串占位: 形如 {@code {{nodeId.paramName}}} 或 {@code {{env.varName}}}。</li>
 * </ul>
 *
 * <p>特殊 {@code nodeId}:</p>
 * <ul>
 *   <li>{@code env} - 环境变量</li>
 *   <li>{@code inputs} / {@code start} - 全局输入(Start 节点入参)</li>
 * </ul>
 *
 * @author Ivan
 */
public final class ParamResolver {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*([\\w.\\-]+)\\.([\\w\\-]+)\\s*}}");

    private ParamResolver() {
    }

    public static Map<String, Object> resolveInputs(List<InputParam> inputs, ExecutionContext ctx) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (inputs == null) {
            return result;
        }
        for (InputParam input : inputs) {
            if (input.getName() == null) {
                continue;
            }
            result.put(input.getName(), resolve(input.getParamMapKey(), ctx));
        }
        return result;
    }

    public static Object resolve(ParamMapKey key, ExecutionContext ctx) {
        if (key == null || key.getNodeId() == null || key.getName() == null) {
            return null;
        }
        return lookup(key.getNodeId(), key.getName(), ctx);
    }

    /** 对字符串中的 {@code {{node.name}}} 占位进行替换。 */
    public static String renderTemplate(String template, ExecutionContext ctx) {
        if (template == null || template.isEmpty()) {
            return template;
        }
        Matcher m = PLACEHOLDER.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            Object val = lookup(m.group(1), m.group(2), ctx);
            m.appendReplacement(sb, Matcher.quoteReplacement(val == null ? "" : String.valueOf(val)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static Object lookup(String nodeId, String name, ExecutionContext ctx) {
        if ("env".equalsIgnoreCase(nodeId)) {
            return ctx.getEnvVars().get(name);
        }
        if ("inputs".equalsIgnoreCase(nodeId) || "start".equalsIgnoreCase(nodeId)) {
            return ctx.getGlobalInputs().get(name);
        }
        NodeOutput output = ctx.getOutput(nodeId);
        if (output == null) {
            return null;
        }
        return output.getOutputs().get(name);
    }
}
