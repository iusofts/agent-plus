package com.iusofts.agentplus.aistat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * AI 用量统计查询请求。
 *
 * @author Ivan
 */
@Data
@Schema(description = "AI 用量统计查询请求")
public class AiUsageStatQueryRequest {

    @Schema(description = "开始日期(按起始日期过滤)")
    private LocalDate startDate;

    @Schema(description = "结束日期(按起始日期过滤)")
    private LocalDate endDate;

    @Schema(description = "起始小时(0-23,可选,精确到小时过滤)")
    private Integer startHour;

    @Schema(description = "结束小时(0-23,可选,精确到小时过滤)")
    private Integer endHour;

    @Schema(description = "聚合粒度:DAY(按天)/HOUR(按小时),默认 DAY")
    private String granularity = "DAY";

    @Schema(description = "组织 ID(可选)")
    private Integer orgId;
}
