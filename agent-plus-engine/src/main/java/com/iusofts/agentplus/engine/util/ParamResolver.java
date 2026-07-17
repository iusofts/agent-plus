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
    private static final Pattern SIMPLE_PLACEHOLDER = Pattern.compile("\\{\\{\\s*([\\w\\-]+)\\s*}}");

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
        return renderTemplate(template, ctx, null);
    }

    /**
     * 对字符串中的占位进行替换。
     *
     * <p>支持两种格式:</p>
     * <ul>
     *   <li>{@code {{node.name}}} - 从上下文查找</li>
     *   <li>{@code {{name}}} - 优先从 {@code localContext} 查找,未命中时从 {@code inputs/start} 查找</li>
     * </ul>
     */
    public static String renderTemplate(String template, ExecutionContext ctx, Map<String, Object> localContext) {
        if (template == null || template.isEmpty()) {
            return template;
        }
        String result = template;

        // 先替换 {{node.name}} 格式
        Matcher m = PLACEHOLDER.matcher(result);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            Object val = lookup(m.group(1), m.group(2), ctx);
            m.appendReplacement(sb, Matcher.quoteReplacement(val == null ? "" : String.valueOf(val)));
        }
        m.appendTail(sb);
        result = sb.toString();

        // 再替换 {{name}} 格式
        m = SIMPLE_PLACEHOLDER.matcher(result);
        sb = new StringBuilder();
        while (m.find()) {
            String name = m.group(1);
            Object val = null;
            if (localContext != null) {
                val = localContext.get(name);
            }
            if (val == null) {
                val = lookup("inputs", name, ctx);
            }
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
