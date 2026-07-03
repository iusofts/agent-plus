package com.iusofts.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 流程运行实例 终止请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
public class AiFlowRuntimeTerminateReqVo {

    @Schema(description = "执行实例ID")
    private Long id;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
