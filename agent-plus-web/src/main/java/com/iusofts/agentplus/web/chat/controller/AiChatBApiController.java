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
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

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
     *
     * <p>不要写 {@code produces = text/event-stream}，否则 Spring 启用严格
     * 内容协商，客户端不带 {@code Accept: text/event-stream} 就会 406。
     * {@link SseEmitter} 在 send 时会自己 reset 响应并设置
     * {@code Content-Type: text/event-stream}，不需要 produces 帮忙。</p>
     */
    @Operation(description = "发送聊天消息(流式)")
    @PostMapping(value = "/sendMessageStream")
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

        // 关键:把流式订阅和消费切到 worker 线程池。
        // 背景:Langchain4jAiChatService#streamChat 内部是异步 I/O + token 回调,
        // 但最后调 future.get() 同步阻塞;而 AiChatServiceImpl#streamChat 用
        // Flux.create 包了它,Flux.create 的 lambda 是在订阅线程(controller 线程)
        // 同步执行的。结果就是 controller 线程被 future.get() 一直阻塞到所有
        // token 处理完,SseEmitter 还没被 Spring 接管成 async,emitter.send 写
        // 的内容全在 Tomcat 内部 buffer,等 controller 释放后一次性 flush,
        // 浏览器看起来就是"一次性返回"。
        //
        // subscribeOn 让 Flux.create 的 lambda 跑在 worker 线程,controller
        // 立即 return;publishOn 把 onNext 统一派发到 worker 线程做 emitter.send,
        // response 写操作单线程、避免 ServletOutputStream 多线程竞争。
        stream = stream
                .subscribeOn(Schedulers.boundedElastic())
                .publishOn(Schedulers.boundedElastic());

        // 超时设大一些，避免长任务被强制关闭；具体时长按业务需求调整
        SseEmitter emitter = new SseEmitter(10L * 60 * 1000L);

        // 关键：用 AtomicReference 包住 Disposable,保证任何收尾路径
        // (客户端断开、流 onError/Complete、emitter 超时/异常/完成)
        // 都能取消上游订阅,避免 AI 调用继续跑、token 继续消耗。
        AtomicReference<Disposable> subRef = new AtomicReference<>();
        Runnable disposeSub = () -> {
            Disposable d = subRef.get();
            if (d != null && !d.isDisposed()) {
                d.dispose();
            }
        };

        subRef.set(stream.subscribe(
            event -> {
                try {
                    log.debug(event.getTimestamp() + "," + event.getType());
                    emitter.send(SseEmitter.event()
                        .name(event.getType())
                        .data(event, MediaType.APPLICATION_JSON));
                } catch (IOException | IllegalStateException e) {
                    // 客户端断开,立刻停掉上游订阅,再结束 emitter
                    disposeSub.run();
                    emitter.completeWithError(e);
                }
            },
            err -> {
                // 流自己报错了(例如上游 AI 调用失败)
                disposeSub.run();
                emitter.completeWithError(err);
            },
            () -> {
                // 正常走完所有事件
                disposeSub.run();
                emitter.complete();
            }
        ));

        // emitter 这一侧的任何收尾都要顺手取消上游订阅
        emitter.onTimeout(() -> {
            disposeSub.run();
            emitter.complete();
        });
        emitter.onError(err -> {
            disposeSub.run();
            emitter.completeWithError(err);
        });
        emitter.onCompletion(disposeSub);
        return emitter;
    }

}