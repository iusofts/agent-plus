/*
 * Copyright (C) 2024 All rights reserved
 * Author: Ivan Shen
 * Date: 2026-05-08
 * Description:AiTokenApplyReqVo.java
 */
package com.iusofts.agentplus.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI令牌申请结果
 * @author Ivan Shen
 */
@Data
public class AiTokenVo {

    @Schema(description = "申请成功")
    private boolean success;

    @Schema(description = "访问令牌")
    private String accessToken;
    
}
