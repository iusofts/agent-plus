/*
 * Copyright (C) 2024 All rights reserved
 * Author: Ivan Shen
 * Date: 2026-05-08
 * Description:AiTokenApplyReqVo.java
 */
package com.iusofts.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI令牌申请参数 
 * @author Ivan Shen
 */
@Data
public class AiTokenApplyReqVo {

    @Schema(description = "来源")
    private Integer source = 0;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;
    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;
    
}
