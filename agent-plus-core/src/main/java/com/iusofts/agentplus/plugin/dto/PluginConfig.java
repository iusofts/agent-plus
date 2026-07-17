package com.iusofts.agentplus.plugin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 插件 配置.
 *
 * @author Ivan
 */
@Data
@Schema(description = "插件配置")
public class PluginConfig {

    @NotBlank(message = "请求地址不能为空")
    @Schema(description = "请求地址")
    private String url;

    @Schema(description = "请求头")
    private Map<String, String> headers;

}

