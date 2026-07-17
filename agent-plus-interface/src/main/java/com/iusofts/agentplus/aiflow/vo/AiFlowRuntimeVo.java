package com.iusofts.agentplus.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 流程运行实例 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
public class AiFlowRuntimeVo {

    @Schema(description = "执行实例ID")
    private Long id;

    @Schema(description = "流程ID")
    private Long flowId;

    @Schema(description = "执行使用的语义化版本v1.0.0")
    private String versionNo;

    @Schema(description = "全局追踪ID")
    private String traceId;

    @Schema(description = "运行状态 0等待 1运行中 2成功 3失败 4终止")
    private Integer runStatus;

    @Schema(description = "是否试运行 0:正式 1:试运行")
    private Integer trialFlag;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "耗时毫秒")
    private Long costMs;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
