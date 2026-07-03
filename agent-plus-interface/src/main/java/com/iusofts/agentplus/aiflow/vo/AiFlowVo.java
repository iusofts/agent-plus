package com.iusofts.agentplus.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * AI流程 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
public class AiFlowVo {

    @Schema(description = "主键ID")
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

    @Schema(description = "当前最新版本号(v1.0.0格式)")
    private String latestVersion;

    @Schema(description = "线上发布版本号，空=未发布")
    private String onlineVersion;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;

}
