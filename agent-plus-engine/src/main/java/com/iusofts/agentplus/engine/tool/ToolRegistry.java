package com.iusofts.agentplus.engine.tool;

import com.iusofts.agentplus.tool.Tool;
import com.iusofts.agentplus.tool.dto.ToolExecuteRequest;
import com.iusofts.agentplus.tool.dto.ToolExecuteResult;

import java.util.HashMap;
import java.util.Map;

/**
 * 工具注册表.
 *
 * @author Ivan
 */
public class ToolRegistry {

    private final Map<String, Tool> tools = new HashMap<>();

    public ToolRegistry register(Tool tool) {
        tools.put(tool.getCode(), tool);
        return this;
    }

    public Tool get(String code) {
        return tools.get(code);
    }

    public boolean hasTool(String code) {
        return tools.containsKey(code);
    }

    public ToolExecuteResult execute(ToolExecuteRequest request) {
        Tool tool = tools.get(request.getToolCode());
        if (tool == null) {
            return ToolExecuteResult.error("未找到工具: " + request.getToolCode());
        }
        return tool.execute(request);
    }
}
