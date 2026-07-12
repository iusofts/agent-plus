package com.iusofts.agentplus.library.vo.tool;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * <p>
 * ai工具 编辑请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-12
 */
@Data
public class AiToolEditReqVo {

    @NotNull(message = "ID不能为空")
    @Schema(description = "编号")
    private Long id;

    @Schema(description = "工具名称")
    private String name;

    @Schema(description = "工具描述")
    private String description;

    @Schema(description = "图标地址")
    private String icon;

    @Schema(description = "参数定义(JSON Schema格式)")
    private Map<String, Object> paramsSchema;

    @Schema(description = "工具配置(JSON格式)")
    private Map<String, Object> config;

    @Schema(description = "启用状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
