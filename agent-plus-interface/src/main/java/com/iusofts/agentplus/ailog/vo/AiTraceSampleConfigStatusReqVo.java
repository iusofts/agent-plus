package com.iusofts.agentplus.ailog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * AI Trace 采样率配置启停请求。
 *
 * @author Ivan
 * @since 2026-08-10
 */
@Data
@Schema(description = "AI Trace 采样率配置启停请求")
public class AiTraceSampleConfigStatusReqVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "ID不能为空")
    private Long id;

    @Schema(description = "启用状态 0:禁用 1:启用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "目标状态不能为空")
    private Integer status;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;
}
