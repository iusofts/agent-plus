package com.iusofts.agentplus.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 运行节点明细 查询请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
public class AiFlowRuntimeNodeQueryReqVo {

    @Schema(description = "关联运行实例ID")
    private Long runtimeId;

}
