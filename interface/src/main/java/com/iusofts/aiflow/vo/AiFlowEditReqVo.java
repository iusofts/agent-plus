package com.iusofts.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * AI流程 编辑请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
public class AiFlowEditReqVo {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "类型 1:工作流Workflow 2:对话流Chatflow")
    private Integer type;

    @Schema(description = "流程名称")
    private String name;

    @Schema(description = "流程描述")
    private String description;

    @Schema(description = "图标地址")
    private String icon;

    @Schema(description = "启用状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
