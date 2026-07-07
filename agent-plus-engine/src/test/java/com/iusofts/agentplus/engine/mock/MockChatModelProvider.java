package com.iusofts.agentplus.engine.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import com.iusofts.agentplus.aiflow.vo.workflow.data.llm.LLMNodeData;
import com.iusofts.agentplus.engine.llm.ChatModelProvider;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 测试用 {@link ChatModelProvider},不发起任何真实网络调用。
 *
 * <p>默认行为:</p>
 * <ul>
 *   <li>outputParams &gt; 1 → 返回合法 JSON,每个字段回显 <code>[mock:{model}] {name} for: {userText}</code>,
 *       方便 {@code LLMNodeExecutor} 走多字段解析分支。</li>
 *   <li>outputParams &le; 1 → 返回 <code>[mock:{model}] {userText}</code>。</li>
 * </ul>
 *
 * <p>需要自定义响应时,通过构造函数注入 {@link BiFunction} 即可(参数为节点数据 + 消息列表)。</p>
 */
public class MockChatModelProvider implements ChatModelProvider {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final BiFunction<LLMNodeData, List<ChatMessage>, String> responder;

    public MockChatModelProvider() {
        this(MockChatModelProvider::defaultResponse);
    }

    public MockChatModelProvider(BiFunction<LLMNodeData, List<ChatMessage>, String> responder) {
        this.responder = responder;
    }

    @Override
    public ChatModel provide(LLMNodeData nodeData) {
        return new ChatModel() {
            @Override
            public ChatResponse chat(ChatRequest chatRequest) {
                String text = responder.apply(nodeData, chatRequest.messages());
                return ChatResponse.builder()
                        .aiMessage(AiMessage.from(text))
                        .build();
            }
        };
    }

    private static String defaultResponse(LLMNodeData data, List<ChatMessage> messages) {
        String userText = extractUserText(messages);
        String tag = "[mock:" + data.getModel() + "]";

        List<OutputParam> outs = data.getOutputParams();
        if (outs != null && outs.size() > 1) {
            Map<String, Object> obj = new LinkedHashMap<>();
            for (OutputParam p : outs) {
                obj.put(p.getName(), tag + " " + p.getName() + " for: " + userText);
            }
            try {
                return JSON.writeValueAsString(obj);
            } catch (Exception e) {
                return tag + " " + userText;
            }
        }
        return tag + " " + userText;
    }

    private static String extractUserText(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage m = messages.get(i);
            if (m instanceof UserMessage um) {
                return um.hasSingleText() ? um.singleText() : String.valueOf(um.contents());
            }
        }
        return String.valueOf(messages.get(messages.size() - 1));
    }
}
