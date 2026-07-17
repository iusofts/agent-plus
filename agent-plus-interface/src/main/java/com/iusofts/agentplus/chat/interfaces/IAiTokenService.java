/*
 * Copyright (C) 2024 All rights reserved
 * Author: Ivan Shen
 * Date: 2026-05-08
 * Description:IAiLimitService.java
 */
package com.iusofts.agentplus.chat.interfaces;

import com.iusofts.agentplus.chat.vo.AiTokenApplyReqVo;
import com.iusofts.agentplus.chat.vo.AiTokenVo;

/**
 *
 * @author Ivan Shen
 */
public interface IAiTokenService {

    AiTokenVo applyToken(AiTokenApplyReqVo reqVo);
    
    boolean useToken(String accessToken);
    
}
