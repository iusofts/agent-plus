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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

import static com.iusofts.agentplus.common.constants.SysConstant.SYSCODE;

@Slf4j
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

    /**
     * 发送聊天消息（流式 SSE）。
     *
     * <p>注意：项目使用 spring-boot-starter-web（Spring MVC），不能直接返回
     * {@code Flux<WorkflowStreamEvent>}，否则 Spring 会把整个流收集成 JSON 数组
     * 一次性返回，浏览器看到的是普通 Response 而不是 EventStream。这里用
     * {@link SseEmitter} 订阅服务层的 Flux，每条事件立刻 send 出去并 flush。</p>
     */
    @Operation(description = "发送聊天消息(流式)")
    @PostMapping(value = "/sendMessageStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessageStream(@ApValidated @RequestBody AiServiceChatReqVo chatReqVo, @BLoginUser BLoginUserVo loginUserVo) {
        chatReqVo.setOrgId(SYSCODE);
        chatReqVo.setOperatorId(loginUserVo.getUser().getUserId());

        // 1:自主规划(AiChatServiceImpl)  2:对话流(FlowChatServiceImpl)
        Integer agentType = chatReqVo.getAgentType();
        Flux<WorkflowStreamEvent> stream;
        if (agentType != null && agentType == 2) {
            stream = flowChatService.streamChat(chatReqVo);
        } else {
            stream = aiChatService.streamChat(chatReqVo);
        }

        // 超时设大一些，避免长任务被强制关闭；具体时长按业务需求调整
        SseEmitter emitter = new SseEmitter(10L * 60 * 1000L);
        stream.subscribe(
            event -> {
                try {
                    log.info(event.getTimestamp() + "," + event.getType());
                    emitter.send(SseEmitter.event()
                        .name(event.getType())
                        .data(event, MediaType.APPLICATION_JSON));
                } catch (IOException | IllegalStateException e) {
                    // 客户端断开连接，停止订阅
                    emitter.completeWithError(e);
                }
            },
            emitter::completeWithError,
            emitter::complete
        );
        // 客户端断开 / 超时也要取消上游订阅
        emitter.onTimeout(() -> emitter.complete());
        emitter.onError(emitter::completeWithError);
        return emitter;
    }

}