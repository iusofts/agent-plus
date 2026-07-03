package com.iusofts.agentplus.ai.vo.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * ai结构消息VO
 *
 * @author AI
 */
@Data
@Schema(name = "AiStructMessageVo", description = "ai结构消息VO")
public class AiStructMessageVo {

    @Schema(description = "回复内容")
    private String content;
    
}