package com.iusofts.agentplus.library.vo.plugin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * ai插件 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-13
 */
@Data
public class AiPluginVo {

    @Schema(description = "主键编号")
    private Long id;

    @Schema(description = "插件名称")
    private String name;

    @Schema(description = "插件唯一编码")
    private String code;

    @Schema(description = "插件类型 1:内置工具 2:服务接口 3:MCP")
    private Integer pluginType;

    @Schema(description = "插件描述")
    private String description;

    @Schema(description = "插件图标地址")
    private String icon;

    @Schema(description = "排序权重")
    private Integer sort;

    @Schema(description = "启用状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "记录最后更新时间")
    private LocalDateTime updateTime;

}
