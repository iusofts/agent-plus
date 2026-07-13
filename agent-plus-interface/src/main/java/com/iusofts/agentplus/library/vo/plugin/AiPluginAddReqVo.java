package com.iusofts.agentplus.library.vo.plugin;

import com.iusofts.agentplus.plugin.dto.PluginConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * ai插件 新增请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-13
 */
@Data
public class AiPluginAddReqVo {

    @NotBlank(message = "插件名称不能为空")
    @Schema(description = "插件名称")
    private String name;

    @NotNull(message = "插件类型不能为空")
    @Schema(description = "插件类型 1:内置工具 2:服务接口 3:MCP")
    private Integer pluginType;

    @Schema(description = "插件描述")
    private String description;

    @Schema(description = "插件图标地址")
    private String icon;

    @Schema(description = "插件专属配置，按类型区分结构")
    private PluginConfig pluginConfig;

    @Schema(description = "排序权重")
    private Integer sort;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
