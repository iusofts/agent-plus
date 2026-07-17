package com.iusofts.agentplus.library.vo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * AI模型配置 数据传输对象(列表)
 * </p>
 *
 * <p>apiKey 为掩码后的值。</p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
@Data
public class AiModelVo {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "模型类型 1:LLM 2:Embedding")
    private Integer modelType;

    @Schema(description = "提供商 qwen:千问 doubao:豆包 openai:OpenAI")
    private String provider;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "模型显示名称")
    private String displayName;

    @Schema(description = "API密钥(已掩码)")
    private String apiKey;

    @Schema(description = "API基础URL")
    private String baseUrl;

    @Schema(description = "状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "是否默认模型 0:否 1:是")
    private Integer isDefault;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "记录最后更新时间")
    private LocalDateTime updateTime;

}
