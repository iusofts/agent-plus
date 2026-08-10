package com.iusofts.agentplus.ailog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * AI Trace 采样率配置修改请求。
 *
 * @author Ivan
 * @since 2026-08-10
 */
@Data
@Schema(description = "AI Trace 采样率配置修改请求")
public class AiTraceSampleConfigEditReqVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID(修改时必填)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "主键ID不能为空")
    private Long id;

    @Schema(description = "配置类型 1:全局 2:组织 3:用户", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "配置类型不能为空")
    private Integer configType;

    @Schema(description = "目标ID(全局=0;组织=orgId;用户=userId)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long targetId;

    @Schema(description = "目标名称(展示/搜索用,组织名/用户昵称/全局占位)")
    private String targetName;

    @Schema(description = "采样率,取值 0.0000 ~ 1.0000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "采样率不能为空")
    @DecimalMin(value = "0.0000", message = "采样率不能小于 0")
    @DecimalMax(value = "1.0000", message = "采样率不能大于 1")
    private BigDecimal sampleRate;

    @Schema(description = "启用状态 0:禁用 1:启用,不传则保持原值")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;
    @Schema(description = "操作人姓名", hidden = true)
    private String operatorName;
}
