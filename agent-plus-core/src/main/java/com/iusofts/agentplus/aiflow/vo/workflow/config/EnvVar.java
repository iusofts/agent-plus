package com.iusofts.agentplus.aiflow.vo.workflow.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 环境变量项
 *
 * @author Ivan
 * @since 2026-07-02
 */
@Data
@Schema(description = "环境变量配置项")
public class EnvVar implements Serializable {

    @Schema(description = "变量名称", example = "apiKey")
    private String name;

    @Schema(description = "变量类型", example = "string")
    private String type;

    @Schema(description = "变量描述", example = "API 密钥")
    private String description;

    @Schema(description = "默认值", example = "xxxxxx")
    private String defaultValue;
}