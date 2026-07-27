package com.iusofts.agentplus.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 流程运行时序列表项(用于下拉框选择)
 * </p>
 *
 * @author Ivan
 */
@Data
public class AiFlowRuntimeTraceListVo {

    @Schema(description = "执行实例ID")
    private Long id;

    @Schema(description = "全局追踪ID")
    private String traceId;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "是否试运行 0:正式 1:流程试运行 2:节点试运行")
    private Integer trialFlag;

    @Schema(description = "运行状态 0等待 1运行中 2成功 3失败 4终止")
    private Integer runStatus;

}
