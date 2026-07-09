package com.iusofts.agentplus.ailog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * LLM 调用日志查询请求。
 *
 * @author Ivan
 */
@Data
@Schema(description = "LLM 调用日志查询请求")
public class LlmCallLogQueryRequest {

    @Schema(description = "链路追踪 ID")
    private String traceId;

    @Schema(description = "调用来源：AGENT/CHAT/FLOW/API")
    private String callSource;

    @Schema(description = "模型 ID")
    private Long modelId;

    @Schema(description = "智能体 ID")
    private Long agentId;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页条数")
    private Integer pageSize = 20;
}
