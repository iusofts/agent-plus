package com.iusofts.agentplus.chat.vo.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * ai智能体 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-03-31
 */
@Data
public class AiAgentVo {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "智能体名称")
    private String name;

    @Schema(description = "功能介绍")
    private String description;

    @Schema(description = "设定描述")
    private String systemPrompt;

    @Schema(description = "使用模型ID")
    private Long modelId;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "记录最后更新时间")
    private LocalDateTime updateTime;

}
