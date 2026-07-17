package com.iusofts.agentplus.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.iusofts.agentplus.plugin.dto.PluginConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * ai插件
 * </p>
 *
 * @author Ivan
 * @since 2026-07-13
 */
@Getter
@Setter
@ToString
@TableName(value = "ai_plugin", autoResultMap = true)
@Schema(name = "AiPlugin", description = "ai插件")
public class AiPlugin implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键编号")
    @TableId(value = "id", type = IdType.NONE)
    private Long id;

    @Schema(description = "插件名称")
    private String name;

    @Schema(description = "插件类型 1:内置工具 2:服务接口 3:MCP")
    private Integer pluginType;

    @Schema(description = "插件描述")
    private String description;

    @Schema(description = "插件图标地址")
    private String icon;

    @Schema(description = "插件专属配置，按类型区分结构")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private PluginConfig pluginConfig;

    @Schema(description = "排序权重")
    private Integer sort;

    @Schema(description = "启用状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "最后更新人ID")
    private Long updateBy;

    @Schema(description = "记录最后更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "删除标记(0:正常 1:已删除)")
    private Boolean deleteFlag;

    @Schema(description = "所属组织ID")
    private Integer orgId;

}
