package com.iusofts.agentplus.web.common.controller;

import com.iusofts.agentplus.basic.web.annotation.BLoginUser;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.freemaker.TemplateManage;
import com.iusofts.agentplus.basic.validation.ApValidated;
import com.iusofts.agentplus.system.vo.BLoginUserVo;
import com.iusofts.agentplus.web.common.vo.TestVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "测试")
public class TestController extends BApiController {

    @Autowired
    private TemplateManage templateManage;

    @Operation(description = "测试")
    @PostMapping("/test")
    public TestVO test(@ApValidated @RequestBody TestVO param) {
        return param;
    }

    @Operation(description = "测试Html")
    @GetMapping("/testHtml")
    public String testHtml() {
        return templateManage.parse("test", null);
    }

    @Operation(description = "测试异常")
    @PostMapping("/testE")
    public void test() {
        throw new SystemBusinessException("测试异常");
    }


    @Operation(description = "测试图形验证码")
    @GetMapping("/testCodes")
    public void testCodes(HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    @Operation(description = "测试")
    @PostMapping("/testLoginUser")
    public BLoginUserVo test(@BLoginUser BLoginUserVo bLoginUserVo) {
        return bLoginUserVo;
    }

    /**
     * SSE 模拟接口：每隔 intervalMs 毫秒推送一条消息，共推送 count 条。
     * <p>用于本地调试 EventSource / 流式调用是否打通，无业务依赖。</p>
     *
     * <p>注意：不要写 {@code produces = text/event-stream}，否则 Spring 启用
     * 严格内容协商，客户端不带 {@code Accept: text/event-stream} 就会 406。
     * {@link SseEmitter} 在 send 时会自己 reset 响应并设置
     * {@code Content-Type: text/event-stream}，不需要 produces 帮忙。</p>
     *
     * @param count      推送条数，默认 10
     * @param intervalMs 推送间隔(毫秒)，默认 1000
     */
    @Operation(description = "SSE 模拟接口")
    @GetMapping(value = "/testSse")
    public SseEmitter testSse(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(defaultValue = "1000") long intervalMs) {
        // 整体超时给大一些，避免长任务被强制关闭
        SseEmitter emitter = new SseEmitter(10L * 60 * 1000L);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("ts", System.currentTimeMillis());
                payload.put("msg", "hello sse " + System.currentTimeMillis());
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(payload, MediaType.APPLICATION_JSON));
            } catch (IllegalStateException | IOException e) {
                // 客户端断开，停止推送
                emitter.completeWithError(e);
                scheduler.shutdownNow();
            }
        }, 0, intervalMs, TimeUnit.MILLISECONDS);

        // 推到指定条数后结束
        scheduler.schedule(() -> {
            future.cancel(false);
            try {
                emitter.send(SseEmitter.event().name("done").data("bye"));
            } catch (IOException ignored) {
                // 客户端已断开
            }
            emitter.complete();
            scheduler.shutdown();
        }, intervalMs * count, TimeUnit.MILLISECONDS);

        // 客户端断开 / 超时也要关掉定时任务，避免线程泄漏
        Runnable cleanup = () -> {
            future.cancel(false);
            scheduler.shutdownNow();
        };
        emitter.onTimeout(cleanup);
        emitter.onError(t -> cleanup.run());
        emitter.onCompletion(cleanup);
        return emitter;
    }

}
