package com.iusofts.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 设置AI智能体为默认智能体请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-04-04
 */
@Data
@Schema(description = "设置AI智能体为默认智能体请求对象")
public class AiAgentSetDefaultReqVo {

    @Schema(description = "智能体ID")
    private Long id;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;
    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}