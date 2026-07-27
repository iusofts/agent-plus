package com.iusofts.agentplus.chat.vo.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 * 根据对话流ID查询智能体的请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-21
 */
@Data
public class AiAgentQueryByChatFlowReqVo {

    @NotNull(message = "对话流ID不能为空")
    @Schema(description = "对话流ID")
    private Long chatFlowId;

    @Schema(description = "所属组织ID", hidden = true)
    private Integer orgId;

}
