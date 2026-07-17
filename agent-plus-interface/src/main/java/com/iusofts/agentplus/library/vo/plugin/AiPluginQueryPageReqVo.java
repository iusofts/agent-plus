package com.iusofts.agentplus.library.vo.plugin;

import com.iusofts.agentplus.basic.web.vo.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * ai插件 分页查询请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiPluginQueryPageReqVo extends PageQuery {

    @Schema(description = "插件名称")
    private String name;

    @Schema(description = "插件编码")
    private String code;

    @Schema(description = "插件类型 1:内置工具 2:服务接口 3:MCP")
    private Integer pluginType;

    @Schema(description = "启用状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

}
