package com.iusofts.agentplus.plugin.vectorstore;

import com.iusofts.agentplus.knowledge.dto.EmbeddingModelDTO;
import com.iusofts.agentplus.knowledge.EmbeddingModelQueryProvider;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库存储服务（封装向量化、存储逻辑，service 层不直接依赖 langchain4j）。
 *
 * @author Ivan
 */
@Component
public class KnowledgeStoreService {

    private static final int EMBED_BATCH_SIZE = 20;

    private final EmbeddingModelQueryProvider embeddingModelQueryProvider;
    private final RedisVectorStoreManager vectorStoreManager;

    public KnowledgeStoreService(
            EmbeddingModelQueryProvider embeddingModelQueryProvider,
            RedisVectorStoreManager vectorStoreManager) {
        this.embeddingModelQueryProvider = embeddingModelQueryProvider;
        this.vectorStoreManager = vectorStoreManager;
    }

    /**
     * 分批次向量化并存储。
     *
     * @param collectionName 集合名称
     * @param vectorIds      向量 ID 列表（与 chunkTexts 一一对应）
     * @param chunkTexts     分块文本列表
     * @param chunkMetadatas 分块元数据列表（与 chunkTexts 一一对应）
     * @param embeddingModelId 嵌入模型 ID
     * @return 实际存储的分块数
     */
    public int batchEmbedAndStore(
            String collectionName,
            List<String> vectorIds,
            List<String> chunkTexts,
            List<Map<String, Object>> chunkMetadatas,
            Long embeddingModelId) {

        EmbeddingModelDTO embeddingModelDTO = embeddingModelQueryProvider.getModel(embeddingModelId);
        EmbeddingModel embeddingModel = EmbeddingModelFactory.createEmbeddingModel(embeddingModelDTO);

        int total = chunkTexts.size();

        for (int from = 0; from < total; from += EMBED_BATCH_SIZE) {
            int to = Math.min(from + EMBED_BATCH_SIZE, total);
            List<String> batchTexts = chunkTexts.subList(from, to);
            List<String> batchVectorIds = vectorIds.subList(from, to);
            List<Map<String, Object>> batchMetadatas = chunkMetadatas.subList(from, to);

            List<TextSegment> segments = new ArrayList<>(batchTexts.size());
            for (int i = 0; i < batchTexts.size(); i++) {
                String content = batchTexts.get(i);
                Map<String, Object> metadataMap = batchMetadatas.get(i);
                segments.add(TextSegment.from(content, Metadata.from(metadataMap)));
            }

            Response<List<Embedding>> response = embeddingModel.embedAll(segments);
            List<Embedding> embeddings = response.content();
            vectorStoreManager.addAll(collectionName, batchVectorIds, embeddings, segments);
        }

        return total;
    }
}
