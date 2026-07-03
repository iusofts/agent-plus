package com.iusofts.agentplus.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * ai智能体 编辑请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-03-31
 */
@Data
public class AiAgentEditReqVo {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "类型 1.问候型 2.销售型 3.鉴别型")
    private Integer type;

    @Schema(description = "智能体名称")
    private String name;

    @Schema(description = "代码")
    private String code;

    @Schema(description = "设定描述")
    private String systemPrompt;

    @Schema(description = "最大轮次")
    private Integer maxRounds;

    @Schema(description = "转人工")
    private String transferHuman;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;
    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

    @Schema(description = "行业id")
    private Long industryId;

}