package com.iusofts.agentplus.ailog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI Trace 采样率配置批量软删除请求。
 *
 * @author Ivan
 * @since 2026-08-10
 */
@Data
@Schema(description = "AI Trace 采样率配置批量删除请求")
public class AiTraceSampleConfigRemoveReqVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "ID集合不能为空")
    private List<Long> ids;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;
}
