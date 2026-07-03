package com.iusofts.agentplus.ai.vo.service;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Ivan Shen
 */
@Data
public class AiServiceChatTestReqVo {

    @Schema(description = "会话id")
    private Long conversationId;

    @NotNull(message = "智能体不能为空")
    @Schema(description = "智能体id")
    private Long agentId;

    @Schema(description = "发送内容")
    private String content;

}
