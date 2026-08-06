package com.iusofts.agentplus.engine.tool;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iusofts.agentplus.tool.Tool;
import com.iusofts.agentplus.tool.ToolQueryProvider;
import com.iusofts.agentplus.tool.dto.ToolDTO;
import com.iusofts.agentplus.tool.dto.ToolExecuteRequest;
import com.iusofts.agentplus.tool.dto.ToolExecuteResult;
import com.iusofts.agentplus.trace.TraceUtil;
import com.iusofts.agentplus.trace.constants.TraceConstant;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;

/**
 * 工具注册表.
 *
 * @author Ivan
 */
public class ToolRegistry {

    private final Map<String, Tool> builtInTools = new HashMap<>();
    private final ToolQueryProvider toolQueryProvider;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ToolRegistry(ToolQueryProvider toolQueryProvider) {
        this.toolQueryProvider = toolQueryProvider;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public ToolRegistry register(Tool tool) {
        builtInTools.put(tool.getName(), tool);
        return this;
    }

    public Tool get(String name) {
        return builtInTools.get(name);
    }

    public boolean hasTool(String name) {
        return builtInTools.containsKey(name);
    }

    public ToolExecuteResult execute(ToolExecuteRequest request) {
        Tool tool = resolveTool(request);
        if (tool == null) {
            return ToolExecuteResult.error("未找到工具: " + request.getToolId());
        }
        // 创建 span 包装工具执行
        final String toolName = tool.getName();
        final Long toolId = request.getToolId();
        return TraceUtil.span(TraceConstant.SPAN_TOOL_EXECUTE_PREFIX + toolName, SpanKind.INTERNAL, span -> {
            TraceUtil.setLabel(toolName);
            if (toolId != null) {
                span.setAttribute(TraceConstant.ATTR_TOOL_ID, toolId);
            }
            // 记录输入参数
            try {
                span.setAttribute(TraceConstant.ATTR_PAYLOAD_INPUT, JSON.toJSONString(request));
            } catch (Exception ignore) {
                // 序列化失败不影响主流程
            }
            ToolExecuteResult result = tool.execute(request);
            // 记录输出结果
            try {
                span.setAttribute(TraceConstant.ATTR_PAYLOAD_OUTPUT, JSON.toJSONString(result));
            } catch (Exception ignore) {
                // 序列化失败不影响主流程
            }
            // 工具执行失败：将 Span 状态置为 ERROR
            if (result != null && !result.isSuccess()) {
                span.setStatus(StatusCode.ERROR, result.getErrorMessage());
            }
            return result;
        });
    }

    private Tool resolveTool(ToolExecuteRequest request) {
        Long id = request.getToolId();
        if (id == null) {
            return null;
        }
        ToolDTO toolDTO = toolQueryProvider.getById(id);
        if (toolDTO == null || toolDTO.getStatus() == null || toolDTO.getStatus() != 1) {
            return null;
        }
        // 内置工具：按名称匹配已注册的实现
        if (toolDTO.getType() != null && toolDTO.getType() == 1) {
            return builtInTools.get(toolDTO.getName());
        }
        // 服务接口工具：HTTP 包装
        if (toolDTO.getType() != null && toolDTO.getType() == 2) {
            return new HttpToolWrapper(toolDTO, httpClient, objectMapper);
        }
        return null;
    }
}
