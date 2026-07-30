package com.iusofts.agentplus.engine.executor.impl;

import com.iusofts.agentplus.aiflow.enums.FlowNodeType;
import com.iusofts.agentplus.aiflow.vo.workflow.Node;
import com.iusofts.agentplus.aiflow.vo.workflow.data.code.CodeNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import com.iusofts.agentplus.engine.context.ExecutionContext;
import com.iusofts.agentplus.engine.context.NodeOutput;
import com.iusofts.agentplus.engine.exception.WorkflowExecutionException;
import com.iusofts.agentplus.engine.executor.NodeExecutor;
import com.iusofts.agentplus.engine.script.GraalJsScriptEngine;
import com.iusofts.agentplus.engine.script.ScriptEngine;
import com.iusofts.agentplus.engine.util.ParamResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 代码节点执行器。
 *
 * @author Ivan
 */
public class CodeNodeExecutor implements NodeExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CodeNodeExecutor.class);

    private final ScriptEngine jsEngine;

    public CodeNodeExecutor() {
        this.jsEngine = new GraalJsScriptEngine();
    }

    @Override
    public FlowNodeType type() {
        return FlowNodeType.CODE;
    }

    @Override
    public NodeOutput execute(Node node, ExecutionContext ctx) throws Exception {
        CodeNodeData data = (CodeNodeData) node.getData();
        if (data == null) {
            throw new WorkflowExecutionException(node.getId(), "Code 节点缺少 data", null);
        }

        // 解析输入参数
        Map<String, Object> inputs = ParamResolver.resolveInputs(data.getInputParams(), ctx);

        // 选择脚本引擎
        ScriptEngine engine = selectEngine(data.getScriptType(), node.getId());

        // 执行脚本
        Map<String, Object> scriptResult;
        try {
            scriptResult = engine.execute(data.getScript(), inputs, data.getTimeout() * 1000);
        } catch (Exception e) {
            LOGGER.error("脚本执行失败, nodeId={}", node.getId(), e);
            throw new WorkflowExecutionException(node.getId(), "脚本执行失败: " + e.getMessage(), e);
        }

        // 映射输出
        Map<String, Object> outputs = mapOutputs(scriptResult, data.getOutputParams());

        return new NodeOutput(node.getId(), outputs);
    }

    private ScriptEngine selectEngine(String scriptType, String nodeId) {
        if (scriptType == null || scriptType.isBlank()) {
            return jsEngine;
        }
        switch (scriptType.toUpperCase(Locale.ROOT)) {
            case "JS":
            case "JAVASCRIPT":
                return jsEngine;
            default:
                throw new WorkflowExecutionException(nodeId, "不支持的脚本类型: " + scriptType, null);
        }
    }

    private Map<String, Object> mapOutputs(Map<String, Object> scriptResult, List<OutputParam> outputParams) {
        Map<String, Object> outputs = new HashMap<>();

        if (outputParams == null || outputParams.isEmpty()) {
            // 没有配置输出参数，直接返回所有脚本结果
            outputs.putAll(scriptResult);
            return outputs;
        }

        // 按配置的输出参数映射
        for (OutputParam param : outputParams) {
            String name = param.getName();
            if (scriptResult.containsKey(name)) {
                outputs.put(name, scriptResult.get(name));
            } else {
                outputs.put(name, null);
            }
        }

        return outputs;
    }

}
