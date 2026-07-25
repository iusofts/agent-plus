package com.iusofts.agentplus.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * Span详情VO
 * </p>
 *
 * @author Ivan
 */
@Data
public class AiSpanDetailVo {

    @Schema(description = "span表主键ID")
    private Long id;

    @Schema(description = "节点展示名称")
    private String label;

    @Schema(description = "持续时长(微秒)")
    private Long dur;

    @Schema(description = "tokens")
    private Long tokens;

    @Schema(description = "分类，工作流为workflow，节点用节点类型(Start/LLM/Knowledge等)，节点子级继承节点类型，用于前端区分显示图标")
    private String cat;

    @Schema(description = "节点ID，业务类型为节点类型时有值")
    private String nodeId;

    @Schema(description = "span状态: OK/ERROR")
    private String status;

    @Schema(description = "错误信息(仅status=ERROR时)")
    private String statusMessage;

    @Schema(description = "节点入参(JSON)")
    private String inputPayload;

    @Schema(description = "节点返回值(JSON)")
    private String outputPayload;

}
