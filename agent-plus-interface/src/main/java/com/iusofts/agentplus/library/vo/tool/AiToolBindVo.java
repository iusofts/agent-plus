package com.iusofts.agentplus.library.vo.tool;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * ai工具 绑定信息数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-13
 */
@Data
public class AiToolBindVo {

    @Schema(description = "工具ID")
    private Long id;

    @Schema(description = "所属插件ID")
    private Long pluginId;

    @Schema(description = "插件名称")
    private String pluginName;

    @Schema(description = "工具名称")
    private String name;

    @Schema(description = "工具描述")
    private String description;

    @Schema(description = "图标地址")
    private String icon;

}
