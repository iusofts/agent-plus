package com.iusofts.agentplus.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * <p>
 * 流程运行时序列表 查询请求对象(用于下拉框选择)
 * </p>
 *
 * @author Ivan
 */
@Data
public class AiFlowRuntimeTraceListReqVo {

    @NotNull(message = "流程版本ID不能为空")
    @Schema(description = "流程版本ID")
    private Long versionId;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "状态 空或0:全部(仅成功和失败) 2:成功 3:失败")
    private Integer status;

}
