package com.iusofts.agentplus.engine.llm;

import com.iusofts.agentplus.aiflow.vo.workflow.data.llm.LLMNodeData;
import com.iusofts.agentplus.llm.dto.ChatMessage;
import com.iusofts.agentplus.llm.dto.ChatResponse;
import com.iusofts.agentplus.llm.dto.ToolDefinition;
import dev.langchain4j.model.chat.ChatModel;

import java.util.List;

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
     * @param modelId 模型ID
     * @param messages 消息列表
     * @param config 模型配置
     * @param tools 工具定义列表（可为 null 表示无工具）
     * @return 聊天响应
     */
    ChatResponse chat(Long modelId, List<ChatMessage> messages,
                      com.iusofts.agentplus.llm.dto.LlmModelConfigDTO config,
                      List<ToolDefinition> tools);
}
