package com.iusofts.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * ai智能体 详情数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-03-31
 */
@Data
public class AiAgentDetailVo {

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

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "记录最后更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "行业id")
    private Long industryId;

    @Schema(description = "行业名称")
    private String industryName;

}