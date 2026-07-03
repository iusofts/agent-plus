package com.iusofts.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * AI流程版本 发布请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
public class AiFlowVersionPublishReqVo {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
