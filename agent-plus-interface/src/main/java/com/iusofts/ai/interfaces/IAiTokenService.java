/*
 * Copyright (C) 2024 All rights reserved
 * Author: Ivan Shen
 * Date: 2026-05-08
 * Description:IAiLimitService.java
 */
package com.iusofts.ai.interfaces;

import com.iusofts.ai.vo.AiTokenApplyReqVo;
import com.iusofts.ai.vo.AiTokenVo;

/**
 *
 * @author Ivan Shen
 */
public interface IAiTokenService {

    AiTokenVo applyToken(AiTokenApplyReqVo reqVo);
    
    boolean useToken(String accessToken);
    
}
