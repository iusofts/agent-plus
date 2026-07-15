package com.iusofts.agentplus.plugin.llm;

import com.iusofts.agentplus.aiflow.vo.workflow.data.llm.LLMNodeData;
import com.iusofts.agentplus.engine.llm.ChatModelProvider;
import com.iusofts.agentplus.llm.dto.LlmModelConfigDTO;
import com.iusofts.agentplus.llm.dto.LlmModelDTO;
import com.iusofts.agentplus.llm.LlmModelQueryProvider;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于数据库的 ChatModelProvider 实现（无 DB 依赖，依赖抽象）。
 *
 * <p>仅负责：入参校验、模型实例缓存、编排调用；不直接操作 Mapper、不写厂商构建逻辑。</p>
 *
 * @author Ivan
 */
@Primary
@Component
public class AiModelChatModelProvider implements ChatModelProvider {

    private final LlmModelQueryProvider modelQueryProvider;

    /**
     * 缓存 key = modelId + "@" + temperature，避免每次调用重建 ChatModel。
     */
    private final ConcurrentMap<String, ChatModel> cache = new ConcurrentHashMap<>();

    public AiModelChatModelProvider(LlmModelQueryProvider modelQueryProvider) {
        this.modelQueryProvider = modelQueryProvider;
    }

    @Override
    public ChatModel provide(LLMNodeData nodeData) {
        if (nodeData == null || nodeData.getModelId() == null) {
            throw new IllegalStateException("LLM 节点未指定模型 (model 为空)");
        }

        Long modelId = nodeData.getModelId();
        Double temperature = nodeData.getTemperature();
        String cacheKey = modelId + "@" + temperature;

        return cache.computeIfAbsent(cacheKey, k -> {
            LlmModelDTO modelDTO = modelQueryProvider.getModel(modelId);
            LlmModelConfigDTO config = new LlmModelConfigDTO();
            config.setTemperature(temperature);
            return LlmModelFactory.createChatModel(modelDTO, config);
        });
    }
}
