package com.iusofts.agentplus.chat.interfaces;

import com.iusofts.agentplus.aiflow.stream.WorkflowStreamEvent;
import com.iusofts.agentplus.chat.vo.AiMessageVo;
import com.iusofts.agentplus.chat.vo.AiServiceChatReqVo;
import reactor.core.publisher.Flux;

/**
 * AI服务
 * @author Ivan Shen
 */
public interface IAiChatServiceInterface {

    AiMessageVo chat(AiServiceChatReqVo reqVo);

    Flux<WorkflowStreamEvent> streamChat(AiServiceChatReqVo chatReqVo);

}
