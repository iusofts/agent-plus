package com.iusofts.agentplus.ai.vo.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Ivan Shen
 */
@Data
public class AiServiceResVo {

    @Schema(description = "会话id")
    private Long conversationId;
    
}
