package com.iusofts.ai.interfaces;

import com.iusofts.ai.vo.service.AiMessageVo;
import com.iusofts.ai.vo.service.AiServiceCallReqVo;
import com.iusofts.ai.vo.service.AiServiceChatReqVo;

/**
 * AI服务 
 * @author Ivan Shen
 */
public interface IAiServiceInterface {

    AiMessageVo chat(AiServiceChatReqVo reqVo);
    
    AiMessageVo call(AiServiceCallReqVo reqVo);
    
}
