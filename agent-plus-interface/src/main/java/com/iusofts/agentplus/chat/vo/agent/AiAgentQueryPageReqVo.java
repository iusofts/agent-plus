package com.iusofts.agentplus.chat.vo.agent;

import com.iusofts.agentplus.basic.web.vo.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * ai智能体 查询分页请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-03-31
 */
@Data
public class AiAgentQueryPageReqVo extends PageQuery {

    @Schema(description = "智能体名称")
    private String name;

    @Schema(description = "类型 1:自主规划 2:对话流")
    private Integer type;

    @Schema(description = "启用状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

}
