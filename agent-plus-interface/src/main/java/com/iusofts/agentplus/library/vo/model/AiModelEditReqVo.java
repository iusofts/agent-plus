package com.iusofts.agentplus.library.vo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 * AI模型配置 编辑请求对象
 * </p>
 *
 * <p>apiKey 传空(null 或空串)时不更新原值,避免掩码回显后被覆盖。</p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
@Data
public class AiModelEditReqVo {

    @NotNull(message = "编号不能为空")
    @Schema(description = "编号")
    private Long id;

    @Schema(description = "模型类型 1:LLM 2:Embedding")
    private Integer modelType;

    @Schema(description = "模型提供商：qwen-阿里云(百炼平台)，doubao-字节跳动(火山引擎)，openai-OpenAI")
    private String provider;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "模型显示名称")
    private String displayName;

    @Schema(description = "API密钥(传空则不修改)")
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
