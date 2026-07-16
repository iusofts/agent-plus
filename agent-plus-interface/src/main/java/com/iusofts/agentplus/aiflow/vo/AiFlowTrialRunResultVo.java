package com.iusofts.agentplus.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 流程试运行 结果对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-16
 */
@Data
@Schema(description = "试运行结果")
public class AiFlowTrialRunResultVo {

    @Schema(description = "运行实例ID")
    private Long runtimeId;

    @Schema(description = "全局追踪ID")
    private String traceId;

    @Schema(description = "运行状态 0等待 1运行中 2成功 3失败 4终止")
    private Integer runStatus;

    @Schema(description = "全局输出结果")
    private Map<String, Object> output;

    @Schema(description = "耗时毫秒")
    private Long costMs;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "各节点执行结果")
    private List<AiFlowTrialNodeResultVo> nodeResults;

}
