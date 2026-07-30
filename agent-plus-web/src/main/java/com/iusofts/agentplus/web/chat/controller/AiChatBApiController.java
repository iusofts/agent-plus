package com.iusofts.agentplus.web.chat.controller;

import com.iusofts.agentplus.basic.validation.ApValidated;
import com.iusofts.agentplus.basic.web.annotation.BLoginUser;
import com.iusofts.agentplus.chat.service.AiChatServiceImpl;
import com.iusofts.agentplus.chat.service.FlowChatServiceImpl;
import com.iusofts.agentplus.chat.vo.AiMessageVo;
import com.iusofts.agentplus.chat.vo.AiServiceChatReqVo;
import com.iusofts.agentplus.aiflow.stream.WorkflowStreamEvent;
import com.iusofts.agentplus.system.vo.BLoginUserVo;
import com.iusofts.agentplus.chat.service.AiConversationServiceImpl;
import com.iusofts.agentplus.web.common.controller.BApiController;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static com.iusofts.agentplus.common.constants.SysConstant.SYSCODE;

@RequestMapping("/bapi/ai/chat")
@RestController
public class AiChatBApiController extends BApiController {

    @Resource
    private AiChatServiceImpl aiChatService;
    @Resource
    private FlowChatServiceImpl flowChatService;

    @Operation(description = "发送聊天消息")
    @PostMapping("/sendMessage")
    public AiMessageVo sendMessage(@ApValidated @RequestBody AiServiceChatReqVo chatReqVo, @BLoginUser BLoginUserVo loginUserVo) {
        chatReqVo.setOrgId(SYSCODE);
        chatReqVo.setOperatorId(loginUserVo.getUser().getUserId());

        // 根据智能体类型选择实现
        // 1:自主规划(AiChatServiceImpl)  2:对话流(FlowChatServiceImpl)
        Integer agentType = chatReqVo.getAgentType();
        if (agentType != null && agentType == 2) {
            return flowChatService.chat(chatReqVo);
        } else {
            return aiChatService.chat(chatReqVo);
        }
    }

    @Operation(description = "发送聊天消息(流式)")
    @PostMapping(value = "/sendMessageStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<WorkflowStreamEvent> sendMessageStream(@ApValidated @RequestBody AiServiceChatReqVo chatReqVo, @BLoginUser BLoginUserVo loginUserVo) {
        chatReqVo.setOrgId(SYSCODE);
        chatReqVo.setOperatorId(loginUserVo.getUser().getUserId());

        // 根据智能体类型选择实现
        // 1:自主规划(AiChatServiceImpl)  2:对话流(FlowChatServiceImpl)
        Integer agentType = chatReqVo.getAgentType();
        if (agentType != null && agentType == 2) {
            return flowChatService.streamChat(chatReqVo);
        } else {
            // 自主规划类型暂不支持流式
            throw new RuntimeException("当前智能体类型暂不支持流式输出");
        }
    }

}