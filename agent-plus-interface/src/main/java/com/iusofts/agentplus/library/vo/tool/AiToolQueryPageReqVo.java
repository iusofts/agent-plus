package com.iusofts.agentplus.library.vo.tool;

import com.iusofts.agentplus.basic.web.vo.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * ai工具 分页查询请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiToolQueryPageReqVo extends PageQuery {

    @Schema(description = "工具名称")
    private String name;

    @Schema(description = "工具编码")
    private String code;

    @Schema(description = "工具类型 1:内置工具 2:自定义工具")
    private Integer type;

    @Schema(description = "启用状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

}
