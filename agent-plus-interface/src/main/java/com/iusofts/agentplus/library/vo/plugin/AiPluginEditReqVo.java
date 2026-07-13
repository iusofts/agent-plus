package com.iusofts.agentplus.library.vo.plugin;

import com.iusofts.agentplus.plugin.dto.PluginConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * ai插件 编辑请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-13
 */
@Data
public class AiPluginEditReqVo {

    @NotNull(message = "ID不能为空")
    @Schema(description = "主键编号")
    private Long id;

    @Schema(description = "插件名称")
    private String name;

    @Schema(description = "插件描述")
    private String description;

    @Schema(description = "插件图标地址")
    private String icon;

    @Schema(description = "插件专属配置，按类型区分结构")
    private PluginConfig pluginConfig;

    @Schema(description = "排序权重")
    private Integer sort;

    @Schema(description = "启用状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
