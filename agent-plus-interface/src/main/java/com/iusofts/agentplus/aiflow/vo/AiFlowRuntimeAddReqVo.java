package com.iusofts.agentplus.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 流程运行实例 添加请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
public class AiFlowRuntimeAddReqVo {

    @Schema(description = "流程ID")
    private Long flowId;

    @Schema(description = "执行使用的语义化版本v1.0.0")
    private String versionNo;

    @Schema(description = "本次执行入参JSON")
    private String inputParams;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
