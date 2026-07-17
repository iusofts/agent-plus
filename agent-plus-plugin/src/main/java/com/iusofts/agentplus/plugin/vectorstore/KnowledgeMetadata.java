package com.iusofts.agentplus.plugin.vectorstore;

import com.iusofts.agentplus.knowledge.dto.KnowledgeChunk;
import dev.langchain4j.data.document.Metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 知识库分块元数据的统一定义（单一真源）。
 *
 * <p>元数据 key 曾以魔法字符串散落在三处:向量库索引声明({@code RedisVectorStoreManager})、
 * 写入({@code KnowledgeIngestionService})、读取({@code RedisKnowledgeRetriever})。
 * 集中到此类后,新增/调整元数据字段只需改动这一个文件,并天然与其他向量库实现共享。</p>
 *
 * @author Ivan
 */
public final class KnowledgeMetadata {

    /** 切片唯一 ID。 */
    public static final String CHUNK_ID = "chunkId";
    /** 归属文档 ID。 */
    public static final String DOCUMENT_ID = "documentId";
    /** 文档标题。 */
    public static final String TITLE = "title";
    /** 文档访问地址。 */
    public static final String SOURCE_URL = "sourceUrl";

    /** 业务中所有会用到的元数据 key(用于向量库建索引时声明可检索/可返回的字段)。 */
    public static final Set<String> KEYS = Set.of(CHUNK_ID, DOCUMENT_ID, TITLE, SOURCE_URL);

    private KnowledgeMetadata() {
    }

    /**
     * 写入端:构建单个分块的元数据 map。
     *
     * <p>对 null 值做保护,避免 langchain4j {@code Metadata.from} 因 null value 抛异常。</p>
     */
    public static Map<String, Object> build(Long chunkId, Long documentId, String title, String sourceUrl) {
        Map<String, Object> metadata = new HashMap<>();
        putIfNotNull(metadata, CHUNK_ID, chunkId);
        putIfNotNull(metadata, DOCUMENT_ID, documentId);
        putIfNotNull(metadata, TITLE, title);
        putIfNotNull(metadata, SOURCE_URL, sourceUrl);
        return metadata;
    }

    /**
     * 读取端:从向量库返回的 {@link Metadata} 填充 {@link KnowledgeChunk} 的通用字段。
     */
    public static void populate(Metadata metadata, KnowledgeChunk chunk) {
        if (metadata == null) {
            return;
        }
        chunk.setChunkId(metadata.getLong(CHUNK_ID));
        chunk.setDocumentId(metadata.getLong(DOCUMENT_ID));
        chunk.setTitle(metadata.getString(TITLE));
        chunk.setSourceUrl(metadata.getString(SOURCE_URL));
    }

    private static void putIfNotNull(Map<String, Object> metadata, String key, Object value) {
        if (value != null) {
            metadata.put(key, value);
        }
    }
}
