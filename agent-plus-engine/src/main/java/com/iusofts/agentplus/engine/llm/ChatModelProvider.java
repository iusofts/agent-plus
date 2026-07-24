package com.iusofts.agentplus.engine.llm;

import com.iusofts.agentplus.aiflow.vo.workflow.data.llm.LLMNodeData;
import com.iusofts.agentplus.llm.dto.AiChatRequest;
import com.iusofts.agentplus.llm.dto.AiChatResponse;
import dev.langchain4j.model.chat.ChatModel;

/**
 * LLM 模型工厂。
 *
 * <p>接入方需要实现本接口，把 {@link LLMNodeData#getModelId()} 指向的模型 id
 * 解析为 LangChain4j 的 {@link ChatModel}（如 OpenAI 兼容渠道、DashScope、Ollama 等）。
 *
 * <p>方案一：业务链路信息通过 OpenTelemetry Span Attributes 传递，
 * 不再需要手动透传 AiTraceContext。
 *
 * @author Ivan
 */
public interface ChatModelProvider {

    /**
     * 根据 LLM 节点数据构造/获取 {@link ChatModel} 实例。
     *
     * @param nodeData LLM 节点配置（含模型 id、温度、重试等）
     * @return 可直接调用的 ChatModel
     */
    ChatModel provide(LLMNodeData nodeData);

    /**
     * 执行聊天请求（支持工具调用），并自动记录日志到 {@code ai_llm_call_log}。
     *
     * <p>链路信息（traceId、来源、操作人）自动从当前 OpenTelemetry Span 获取，
     * 调用方需在调用前通过 {@link com.iusofts.agentplus.trace.TraceUtil} 设置属性。
     *
     * @param request 聊天请求参数（模型 id、消息、配置、工具）
     * @return 聊天响应
     */
    AiChatResponse chat(AiChatRequest request);
}
