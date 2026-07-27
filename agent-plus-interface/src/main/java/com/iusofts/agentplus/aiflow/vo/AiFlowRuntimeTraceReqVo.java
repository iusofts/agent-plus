package com.iusofts.agentplus.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * <p>
 * 流程运行时序火焰图 查询请求对象
 * </p>
 *
 * @author Ivan
 */
@Data
public class AiFlowRuntimeTraceReqVo {

    @NotBlank(message = "traceId不能为空")
    @Schema(description = "全局追踪ID")
    private String traceId;

}
