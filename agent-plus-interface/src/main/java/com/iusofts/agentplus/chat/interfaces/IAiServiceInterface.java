package com.iusofts.agentplus.chat.interfaces;

import com.iusofts.agentplus.chat.vo.AiMessageVo;
import com.iusofts.agentplus.chat.vo.AiServiceCallReqVo;
import com.iusofts.agentplus.chat.vo.AiServiceChatReqVo;

/**
 * AI服务 
 * @author Ivan Shen
 */
public interface IAiServiceInterface {

    AiMessageVo chat(AiServiceChatReqVo reqVo);
    
}
