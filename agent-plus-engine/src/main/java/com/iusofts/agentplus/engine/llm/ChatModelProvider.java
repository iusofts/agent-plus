package com.iusofts.agentplus.engine.llm;

import com.iusofts.agentplus.aiflow.vo.workflow.data.llm.LLMNodeData;
import com.iusofts.agentplus.ailog.dto.AiTraceContext;
import com.iusofts.agentplus.llm.dto.AiChatRequest;
import com.iusofts.agentplus.llm.dto.AiChatResponse;
import dev.langchain4j.model.chat.ChatModel;

/**
 * LLM 模型工厂。
 *
 * <p>接入方需要实现本接口,把 {@link LLMNodeData#getModelId()} 指向的模型 id
 * 解析为 LangChain4j 的 {@link ChatModel}(如 OpenAI 兼容渠道、DashScope、Ollama 等)。</p>
 *
 * @author Ivan
 */
public interface ChatModelProvider {

    /**
     * 根据 LLM 节点数据构建/获取 {@link ChatModel} 实例。
     *
     * @param nodeData LLM 节点配置(含模型 id、温度、重试等)
     * @return 可直接调用的 ChatModel
     */
    ChatModel provide(LLMNodeData nodeData);

    /**
     * 执行聊天请求（支持工具调用）。
     *
     * @param request 聊天请求参数（模型 id、消息、配置、工具）
     * @return 聊天响应
     */
    AiChatResponse chat(AiChatRequest request);

    /**
     * 执行聊天请求，并由实现方将本次大模型调用统一落库到 {@code ai_llm_call_log}。
     *
     * <p>调用发生在底层，底层拿不到用户与链路信息，故由调用方（聊天 / 流程）
     * 构造 {@link AiTraceContext} 透传 traceId、来源与操作人。默认忽略上下文，
     * 委托 {@link #chat(AiChatRequest)}，由接入方按需覆盖以记录日志。</p>
     *
     * @param request 聊天请求参数（模型 id、消息、配置、工具）
     * @param ctx     调用链路上下文（可为 null，表示不记录日志）
     * @return 聊天响应
     */
    default AiChatResponse chat(AiChatRequest request, AiTraceContext ctx) {
        return chat(request);
    }
}
