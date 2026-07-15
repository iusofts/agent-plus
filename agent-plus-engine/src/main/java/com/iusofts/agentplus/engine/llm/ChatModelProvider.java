package com.iusofts.agentplus.engine.llm;

import com.iusofts.agentplus.aiflow.vo.workflow.data.llm.LLMNodeData;
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
     * @param nodeData LLM 节点配置(含模型 id、温度、超时、重试等)
     * @return 可直接调用的 ChatModel
     */
    ChatModel provide(LLMNodeData nodeData);
}
