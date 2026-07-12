package com.iusofts.agentplus.engine.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iusofts.agentplus.tool.Tool;
import com.iusofts.agentplus.tool.ToolQueryProvider;
import com.iusofts.agentplus.tool.dto.HttpConfig;
import com.iusofts.agentplus.tool.dto.ToolDTO;
import com.iusofts.agentplus.tool.dto.ToolExecuteRequest;
import com.iusofts.agentplus.tool.dto.ToolExecuteResult;

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
        builtInTools.put(tool.getCode(), tool);
        return this;
    }

    public Tool get(String code) {
        if (builtInTools.containsKey(code)) {
            return builtInTools.get(code);
        }
        return createHttpTool(code);
    }

    public boolean hasTool(String code) {
        if (builtInTools.containsKey(code)) {
            return true;
        }
        ToolDTO tool = toolQueryProvider.getByCode(code);
        return tool != null && tool.getStatus() == 1;
    }

    public ToolExecuteResult execute(ToolExecuteRequest request) {
        Tool tool = get(request.getToolCode());
        if (tool == null) {
            return ToolExecuteResult.error("未找到工具: " + request.getToolCode());
        }
        return tool.execute(request);
    }

    private Tool createHttpTool(String code) {
        ToolDTO toolDTO = toolQueryProvider.getByCode(code);
        if (toolDTO == null || toolDTO.getStatus() == null || toolDTO.getStatus() != 1 || toolDTO.getType() != 2) {
            return null;
        }
        return new HttpToolWrapper(toolDTO, httpClient, objectMapper);
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
        public String getCode() {
            return toolDTO.getCode();
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
