package com.iusofts.agentplus.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * AI流程 添加请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
public class AiFlowAddReqVo {

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

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
