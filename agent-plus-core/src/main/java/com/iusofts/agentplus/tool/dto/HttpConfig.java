package com.iusofts.agentplus.tool.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * HTTP 配置.
 *
 * @author Ivan
 */
@Data
@Schema(description = "HTTP配置")
public class HttpConfig {

    @NotBlank(message = "请求地址不能为空")
    @Schema(description = "请求地址")
    private String url;

    @Schema(description = "请求方法")
    private String method;

    @Schema(description = "请求头")
    private Map<String, String> headers;

    @Schema(description = "超时时间（毫秒）")
    private Integer timeout;

}

