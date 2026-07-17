package com.iusofts.agentplus.aiflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * AI流程主表
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Getter
@Setter
@ToString
@TableName("ai_flow")
@Schema(name = "AiFlow", description = "AI流程主表")
public class AiFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.NONE)
    private Long id;

    @Schema(description = "类型 1:工作流Workflow 2:对话流Chatflow")
    private Integer type;

    @Schema(description = "流程名称")
    private String name;

    @Schema(description = "流程唯一编码")
    private String code;

    @Schema(description = "流程描述")
    private String description;

    @Schema(description = "图标地址")
    private String icon;

    @Schema(description = "启用状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "发布状态 0:未发布 1:已发布")
    private Integer publishStatus;

    @Schema(description = "当前最新版本号(v1.0.0格式)")
    private String latestVersion;

    @Schema(description = "线上发布版本号，空=未发布")
    private String onlineVersion;

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "最后更新人ID")
    private Long updateBy;

    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "软删除 0正常 1删除")
    private Integer deleteFlag;

}
