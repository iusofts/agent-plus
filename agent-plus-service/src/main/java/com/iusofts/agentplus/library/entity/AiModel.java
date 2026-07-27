package com.iusofts.agentplus.library.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * AI模型配置
 * </p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
@Getter
@Setter
@ToString
@TableName("ai_model")
@Schema(name = "AiModel", description = "AI模型配置")
public class AiModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "编号")
    @TableId(value = "id", type = IdType.NONE)
    private Long id;

    @Schema(description = "模型类型 1:LLM 2:Embedding")
    private Integer modelType;

    @Schema(description = "模型提供商：qwen-阿里云(百炼平台)，doubao-字节跳动(火山引擎)，openai-OpenAI")
    private String provider;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "模型显示名称")
    private String displayName;

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

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "最后更新人ID")
    private Long updateBy;

    @Schema(description = "记录最后更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "删除标记(0:正常 1:已删除)")
    private Boolean deleteFlag;

    @Schema(description = "所属组织ID")
    private Integer orgId;
}
