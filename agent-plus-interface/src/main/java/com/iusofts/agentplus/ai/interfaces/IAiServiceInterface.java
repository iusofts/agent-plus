package com.iusofts.agentplus.ai.interfaces;

import com.iusofts.agentplus.ai.vo.service.AiMessageVo;
import com.iusofts.agentplus.ai.vo.service.AiServiceCallReqVo;
import com.iusofts.agentplus.ai.vo.service.AiServiceChatReqVo;

/**
 * AI服务 
 * @author Ivan Shen
 */
public interface IAiServiceInterface {

    AiMessageVo chat(AiServiceChatReqVo reqVo);
    
    AiMessageVo call(AiServiceCallReqVo reqVo);
    
}
