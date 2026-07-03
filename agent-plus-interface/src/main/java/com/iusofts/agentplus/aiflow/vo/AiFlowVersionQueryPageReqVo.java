package com.iusofts.agentplus.aiflow.vo;

import com.iusofts.agentplus.basic.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * AI流程版本 查询分页请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
public class AiFlowVersionQueryPageReqVo extends PageQuery {

    @Schema(description = "关联ai_flow主键")
    private Long flowId;

    @Schema(description = "发布状态 0草稿 1已发布 2待审核")
    private Integer publishingStatus;

}
