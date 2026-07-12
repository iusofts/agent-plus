package com.iusofts.agentplus.library.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.iusofts.agentplus.tool.dto.HttpConfig;
import com.iusofts.agentplus.tool.dto.ToolParam;
import com.iusofts.agentplus.tool.dto.ToolResponseParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * ai工具
 * </p>
 *
 * @author Ivan
 * @since 2026-07-12
 */
@Getter
@Setter
@ToString
@TableName(value = "ai_tool", autoResultMap = true)
@Schema(name = "AiTool", description = "ai工具")
public class AiTool implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "编号")
    @TableId(value = "id", type = IdType.NONE)
    private Long id;

    @Schema(description = "工具名称")
    private String name;

    @Schema(description = "工具唯一编码")
    private String code;

    @Schema(description = "工具类型 1:内置工具 2:HTTP工具")
    private Integer type;

    @Schema(description = "工具描述")
    private String description;

    @Schema(description = "图标地址")
    private String icon;

    @Schema(description = "参数定义列表")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ToolParam> paramsSchema;

    @Schema(description = "响应定义列表")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ToolResponseParam> responseSchema;

    @Schema(description = "HTTP配置")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private HttpConfig httpConfig;

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
