package com.iusofts.agentplus.aiflow.vo;

import com.iusofts.agentplus.basic.web.vo.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * AI Trace 根 Span 分页查询请求对象
 * </p>
 *
 * <p>仅查询 parent_span_id 等于 {@code ROOT_SPAN_ID} 的根 Span 列表，
 * 默认按 start_time 倒序排序，常用于 Trace 检索页面顶部"链路列表"。</p>
 *
 * @author Ivan
 * @since 2026-08-07
 */
@Data
public class AiTraceSpanPageReqVo extends PageQuery {

    @Schema(description = "链路追踪ID(模糊匹配)")
    private String traceId;

    @Schema(description = "span名称(模糊匹配)")
    private String spanName;

    @Schema(description = "span状态: OK/ERROR")
    private String status;

    @Schema(description = "组织ID")
    private Integer orgId;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "试运行标记 0:正式 1:试运行")
    private Integer trialFlag;

}
