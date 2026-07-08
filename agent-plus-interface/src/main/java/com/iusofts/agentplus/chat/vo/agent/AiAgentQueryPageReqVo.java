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

    @Schema(description = "类型 1.问候型 2.销售型 3.鉴别型")
    private Integer type;

    @Schema(description = "智能体名称")
    private String name;

    @Schema(description = "是否默认智能体 0:否 1:是")
    private Integer isDefault;

    @Schema(description = "是否系统预制 0:用户自定义 1:系统内置不可删改")
    private Integer isSystem;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

    @Schema(description = "行业id")
    private Long industryId;
    
}