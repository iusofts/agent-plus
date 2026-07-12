package com.iusofts.agentplus.plugin.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iusofts.agentplus.tool.Tool;
import com.iusofts.agentplus.tool.dto.ToolExecuteRequest;
import com.iusofts.agentplus.tool.dto.ToolExecuteResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 调用工具.
 *
 * @author Ivan
 */
@Component
public class HttpTool implements Tool {

    private static final String CODE = "http";
    private static final String NAME = "HTTP调用";
    private static final String DESCRIPTION = "发送HTTP请求调用外部接口";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HttpTool() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ToolExecuteResult execute(ToolExecuteRequest request) {
        Map<String, Object> params = request.getParams();
        if (params == null) {
            return ToolExecuteResult.error("参数不能为空");
        }

        String url = params.get("url") != null ? params.get("url").toString() : null;
        if (url == null || url.isBlank()) {
            return ToolExecuteResult.error("URL不能为空");
        }

        String method = params.get("method") != null ? params.get("method").toString() : "GET";
        int timeout = params.get("timeout") != null
            ? Integer.parseInt(params.get("timeout").toString())
            : 30;

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(timeout));

            Map<String, String> headers = params.get("headers") instanceof Map
                ? (Map<String, String>) params.get("headers")
                : null;
            if (headers != null) {
                headers.forEach(builder::header);
            }

            String body = params.get("body") != null ? params.get("body").toString() : null;

            switch (method.toUpperCase()) {
                case "POST":
                    builder.POST(body != null
                        ? HttpRequest.BodyPublishers.ofString(body)
                        : HttpRequest.BodyPublishers.noBody());
                    break;
                case "PUT":
                    builder.PUT(body != null
                        ? HttpRequest.BodyPublishers.ofString(body)
                        : HttpRequest.BodyPublishers.noBody());
                    break;
                case "DELETE":
                    builder.DELETE();
                    break;
                case "GET":
                default:
                    builder.GET();
                    break;
            }

            HttpResponse<String> response = httpClient.send(
                builder.build(),
                HttpResponse.BodyHandlers.ofString()
            );

            Map<String, Object> result = new HashMap<>();
            result.put("statusCode", response.statusCode());
            result.put("headers", response.headers().map());
            result.put("body", response.body());

            return ToolExecuteResult.success(result);
        } catch (Exception e) {
            return ToolExecuteResult.error("HTTP请求失败: " + e.getMessage());
        }
    }
}
