package com.iusofts.agentplus.chat.vo.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 * ai智能体 状态变更请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-16
 */
@Data
public class AiAgentStatusReqVo {

    @NotNull(message = "ID不能为空")
    @Schema(description = "主键编号")
    private Long id;

    @NotNull(message = "目标状态不能为空")
    @Schema(description = "启用状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
