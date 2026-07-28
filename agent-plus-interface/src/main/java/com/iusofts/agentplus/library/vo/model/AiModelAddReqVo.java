package com.iusofts.agentplus.library.vo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 * AI模型配置 添加请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
@Data
public class AiModelAddReqVo {

    @NotNull(message = "模型类型不能为空")
    @Schema(description = "模型类型 1:LLM 2:Embedding")
    private Integer modelType;

    @NotBlank(message = "提供商不能为空")
    @Schema(description = "模型提供商：dashscope-阿里云(百炼平台)，volcengine-字节跳动(火山引擎)，openai-OpenAI")
    private String provider;

    @NotBlank(message = "模型名称不能为空")
    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "模型显示名称")
    private String displayName;

    @Schema(description = "模型图标")
    private String icon;

    @NotBlank(message = "API密钥不能为空")
    @Schema(description = "API密钥")
    private String apiKey;

    @Schema(description = "API基础URL")
    private String baseUrl;

    @Schema(description = "模型配置JSON")
    private String config;

    @Schema(description = "状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "是否默认模型 0:否 1:是")
    private Integer isDefault;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
