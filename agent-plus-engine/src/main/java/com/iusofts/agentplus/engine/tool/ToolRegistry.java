package com.iusofts.agentplus.engine.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iusofts.agentplus.tool.Tool;
import com.iusofts.agentplus.tool.ToolQueryProvider;
import com.iusofts.agentplus.tool.dto.HttpConfig;
import com.iusofts.agentplus.tool.dto.ToolDTO;
import com.iusofts.agentplus.tool.dto.ToolExecuteRequest;
import com.iusofts.agentplus.tool.dto.ToolExecuteResult;
import com.iusofts.agentplus.trace.TraceUtil;
import io.opentelemetry.api.trace.SpanKind;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
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
        return TraceUtil.span("tool." + toolName, SpanKind.INTERNAL, span -> {
            TraceUtil.setLabel(toolName);
            if (toolId != null) {
                span.setAttribute("ai.tool_id", toolId);
            }
            return tool.execute(request);
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

    private static class HttpToolWrapper implements Tool {
        private final ToolDTO toolDTO;
        private final HttpClient httpClient;
        private final ObjectMapper objectMapper;

        public HttpToolWrapper(ToolDTO toolDTO, HttpClient httpClient, ObjectMapper objectMapper) {
            this.toolDTO = toolDTO;
            this.httpClient = httpClient;
            this.objectMapper = objectMapper;
        }

        @Override
        public String getName() {
            return toolDTO.getName();
        }

        @Override
        public String getDescription() {
            return toolDTO.getDescription();
        }

        @Override
        public ToolExecuteResult execute(ToolExecuteRequest request) {
            try {
                HttpConfig httpConfig = toolDTO.getHttpConfig();
                if (httpConfig == null || httpConfig.getUrl() == null || httpConfig.getUrl().isBlank()) {
                    return ToolExecuteResult.error("URL 未配置");
                }

                String url = httpConfig.getUrl();
                String method = httpConfig.getMethod() != null ? httpConfig.getMethod().toUpperCase() : "GET";
                int timeout = httpConfig.getTimeout() != null ? httpConfig.getTimeout() : 30000;

                HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofMillis(timeout));

                Map<String, String> headers = httpConfig.getHeaders();
                if (headers != null) {
                    headers.forEach(builder::header);
                }

                String body = request.getParams() != null ? objectMapper.writeValueAsString(request.getParams()) : null;
                switch (method) {
                    case "POST":
                        builder.POST(body != null ? HttpRequest.BodyPublishers.ofString(body) : HttpRequest.BodyPublishers.noBody());
                        break;
                    case "PUT":
                        builder.PUT(body != null ? HttpRequest.BodyPublishers.ofString(body) : HttpRequest.BodyPublishers.noBody());
                        break;
                    case "DELETE":
                        builder.DELETE();
                        break;
                    case "GET":
                    default:
                        builder.GET();
                        break;
                }

                HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

                Map<String, Object> result = new HashMap<>();
                result.put("statusCode", response.statusCode());
                result.put("headers", response.headers().map());
                result.put("body", response.body());

                return ToolExecuteResult.success(result);
            } catch (Exception e) {
                return ToolExecuteResult.error("HTTP 请求失败: " + e.getMessage());
            }
        }
    }
}
