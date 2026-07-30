package com.iusofts.agentplus.llm;

import com.iusofts.agentplus.llm.dto.AiChatMessage;
import com.iusofts.agentplus.llm.dto.AiChatResponse;
import com.iusofts.agentplus.llm.dto.LlmModelConfigDTO;
import com.iusofts.agentplus.llm.dto.ToolDefinition;

import java.util.List;

/**
 * AI 聊天服务接口。
 *
 * <p>业务侧统一使用这个接口调用 LLM，不依赖具体 SDK。</p>
 */
public interface AiChatService {

    /**
     * 聊天。
     *
     * @param messages 消息列表
     * @param modelId  模型 ID
     * @param config   生成参数配置 (可为 null，使用模型默认)
     * @return AI 响应
     */
    default AiChatResponse chat(List<AiChatMessage> messages, Long modelId, LlmModelConfigDTO config) {
        return chat(messages, modelId, config, null);
    }

    /**
     * 聊天（支持工具调用 / function calling）。
     *
     * @param messages 消息列表
     * @param modelId  模型 ID
     * @param config   生成参数配置 (可为 null，使用模型默认)
     * @param tools    可用工具规格列表 (可为 null 或空，表示不启用工具调用)
     * @return AI 响应，若 {@link AiChatResponse#getToolCalls()} 非空则需执行工具后再次调用
     */
    AiChatResponse chat(List<AiChatMessage> messages, Long modelId, LlmModelConfigDTO config, List<ToolDefinition> tools);

    /**
     * 流式聊天（支持 token 回调）。
     *
     * @param messages 消息列表
     * @param modelId  模型 ID
     * @param config   生成参数配置 (可为 null，使用模型默认)
     * @param tokenCallback 每个 token 的回调，参数为当前 token 和已累计的内容
     * @return AI 响应（包含完整内容和 token 使用量）
     */
    default AiChatResponse streamChat(List<AiChatMessage> messages, Long modelId, LlmModelConfigDTO config,
                                       java.util.function.Consumer<String> tokenCallback) {
        return streamChat(messages, modelId, config, null, tokenCallback);
    }

    /**
     * 流式聊天（支持工具调用 / function calling，支持 token 回调）。
     *
     * @param messages 消息列表
     * @param modelId  模型 ID
     * @param config   生成参数配置 (可为 null，使用模型默认)
     * @param tools    可用工具规格列表 (可为 null 或空，表示不启用工具调用)
     * @param tokenCallback 每个 token 的回调，参数为当前 token
     * @return AI 响应，若 {@link AiChatResponse#getToolCalls()} 非空则需执行工具后再次调用
     */
    AiChatResponse streamChat(List<AiChatMessage> messages, Long modelId, LlmModelConfigDTO config,
                               List<ToolDefinition> tools, java.util.function.Consumer<String> tokenCallback);
}
