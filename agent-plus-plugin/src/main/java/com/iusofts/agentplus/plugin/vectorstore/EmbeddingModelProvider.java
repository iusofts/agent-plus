package com.iusofts.agentplus.plugin.vectorstore;

import com.iusofts.agentplus.knowledge.dto.EmbeddingModelDTO;
import com.iusofts.agentplus.knowledge.EmbeddingModelQueryProvider;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 嵌入模型提供者（无 DB 依赖，依赖抽象）。
 *
 * @author Ivan
 */
@Component
public class EmbeddingModelProvider {

    private final EmbeddingModelQueryProvider modelQueryProvider;

    /**
     * 按 modelId 缓存，避免重复构建。
     */
    private final ConcurrentMap<Long, EmbeddingModel> cache = new ConcurrentHashMap<>();

    public EmbeddingModelProvider(EmbeddingModelQueryProvider modelQueryProvider) {
        this.modelQueryProvider = modelQueryProvider;
    }

    /**
     * 获取指定嵌入模型。
     *
     * @param modelId 模型 ID
     * @return 就绪的 EmbeddingModel
     */
    public EmbeddingModel provide(Long modelId) {
        return cache.computeIfAbsent(modelId, id -> {
            EmbeddingModelDTO modelDTO = modelQueryProvider.getModel(id);
            return EmbeddingModelFactory.createEmbeddingModel(modelDTO);
        });
    }
}
