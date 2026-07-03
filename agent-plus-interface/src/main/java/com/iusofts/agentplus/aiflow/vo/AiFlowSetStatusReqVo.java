package com.iusofts.agentplus.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * AI流程 设置状态请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
public class AiFlowSetStatusReqVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "启用状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
