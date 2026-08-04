package com.iusofts.agentplus.engine.util;

import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import com.iusofts.agentplus.engine.context.ExecutionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板占位符解析器。
 *
 * <p>扫描 {@code {{nodeId.name}}} 格式的占位符,支持两种语义:</p>
 * <ul>
 *   <li>{@code {{nodeId.name}}} - 引用某个节点 outputs 中的字段(由 ParamResolver 渲染时查 ctx)</li>
 *   <li>{@code {{name}}} - 简单占位符,优先从 localContext 查找,未命中时从 inputs 查找</li>
 * </ul>
 *
 * <p>本类只负责<b>识别和提取</b>占位符;渲染由 {@link ParamResolver#renderTemplate} 完成。
 * 流式三段式(prefix + 流 + suffix)逻辑已弃用,见历史 commit。</p>
 *
 * @author Ivan
 */
public final class TemplateRenderer {

    /** 占位符正则:{{nodeId.name}} */
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*([\\w.\\-]+)\\.[\\s]*}}");

    private TemplateRenderer() {
    }

    /**
     * 提取模板中所有 {@code {{nodeId.name}}} 占位符对应的 nodeId 列表(按出现顺序,去重)。
     * 主要用于诊断/展示;实际渲染由 ParamResolver 完成。
     */
    public static List<String> extractNodeIds(String template) {
        List<String> ids = new ArrayList<>();
        if (template == null || template.isEmpty()) {
            return ids;
        }
        Matcher m = PLACEHOLDER.matcher(template);
        while (m.find()) {
            String nodeId = m.group(1);
            if (nodeId != null && !ids.contains(nodeId)) {
                ids.add(nodeId);
            }
        }
        return ids;
    }

    /**
     * 渲染模板(简单封装,带 localContext)。
     * 等价于 {@code ParamResolver.renderTemplate(template, ctx, localContext)}。
     */
    public static String render(String template, ExecutionContext ctx, java.util.Map<String, Object> localContext) {
        return ParamResolver.renderTemplate(template, ctx, localContext);
    }

    /**
     * 工具:从 OutputParam 列表中收集所有引用的 nodeId 列表(去重)。
     */
    public static List<String> collectReferencedNodeIds(List<OutputParam> outputParams) {
        List<String> ids = new ArrayList<>();
        if (outputParams == null) {
            return ids;
        }
        for (OutputParam p : outputParams) {
            if (p.getParamMapKey() == null || p.getParamMapKey().getNodeId() == null) {
                continue;
            }
            String nodeId = p.getParamMapKey().getNodeId();
            if (!ids.contains(nodeId)) {
                ids.add(nodeId);
            }
        }
        return ids;
    }
}
