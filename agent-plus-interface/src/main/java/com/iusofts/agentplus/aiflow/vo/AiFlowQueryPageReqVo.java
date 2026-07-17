package com.iusofts.agentplus.aiflow.vo;

import com.iusofts.agentplus.basic.web.vo.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * AI流程 查询分页请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
public class AiFlowQueryPageReqVo extends PageQuery {

    @Schema(description = "类型 1:工作流Workflow 2:对话流Chatflow")
    private Integer type;

    @Schema(description = "流程名称")
    private String name;

    @Schema(description = "启用状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "发布状态 0:未发布 1:已发布")
    private Integer publishStatus;

}
