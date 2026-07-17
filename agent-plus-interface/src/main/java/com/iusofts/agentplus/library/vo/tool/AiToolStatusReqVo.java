package com.iusofts.agentplus.library.vo.tool;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 * ai工具 状态变更请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-13
 */
@Data
public class AiToolStatusReqVo {

    @NotNull(message = "ID不能为空")
    @Schema(description = "编号")
    private Long id;

    @NotNull(message = "目标状态不能为空")
    @Schema(description = "启用状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
