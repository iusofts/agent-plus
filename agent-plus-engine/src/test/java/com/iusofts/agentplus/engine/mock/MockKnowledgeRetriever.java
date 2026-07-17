package com.iusofts.agentplus.engine.mock;

import com.iusofts.agentplus.engine.knowledge.KnowledgeRetriever;
import com.iusofts.agentplus.knowledge.dto.KnowledgeChunk;
import com.iusofts.agentplus.knowledge.dto.KnowledgeRetrieveResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 测试用 {@link KnowledgeRetriever},不接任何真实向量库。
 *
 * <p>默认行为:返回 <code>topK</code> 条形如
 * <code>[mock kb:{knowledgeId}] chunk {i} for query: {query}</code> 的字符串。</p>
 *
 * <p>需要固定回复时通过构造函数传入 <code>Map&lt;knowledgeId, chunks&gt;</code>,
 * 检索时按 topK 截断该列表。</p>
 */
public class MockKnowledgeRetriever implements KnowledgeRetriever {

    private final Map<Long, List<String>> cannedResponses;

    public MockKnowledgeRetriever() {
        this(Collections.emptyMap());
    }

    public MockKnowledgeRetriever(Map<Long, List<String>> cannedResponses) {
        this.cannedResponses = cannedResponses == null ? Collections.emptyMap() : cannedResponses;
    }

    @Override
    public KnowledgeRetrieveResult retrieve(Long knowledgeId, String query, int topK) {
        int limit = Math.max(1, topK);
        List<String> canned = cannedResponses.get(knowledgeId);

        List<String> chunkContents;
        if (canned != null && !canned.isEmpty()) {
            chunkContents = canned.subList(0, Math.min(limit, canned.size()));
        } else {
            chunkContents = new ArrayList<>(limit);
            for (int i = 0; i < limit; i++) {
                chunkContents.add("[mock kb:" + knowledgeId + "] chunk " + i + " for query: " + query);
            }
        }

        List<KnowledgeChunk> chunks = new ArrayList<>();
        for (int i = 0; i < chunkContents.size(); i++) {
            KnowledgeChunk chunk = new KnowledgeChunk();
            chunk.setChunkId((long) i);
            chunk.setContent(chunkContents.get(i));
            chunk.setScore(1.0 - (i * 0.1));
            chunks.add(chunk);
        }

        String contextText = chunks.stream()
                .map(KnowledgeChunk::getContent)
                .collect(Collectors.joining("\n\n"));

        KnowledgeRetrieveResult result = new KnowledgeRetrieveResult();
        result.setSuccess(true);
        result.setQuery(query);
        result.setRewriteQuery(query);
        result.setChunks(chunks);
        result.setContextText(contextText);
        result.setTotalHit(chunks.size());
        result.setHasResult(!chunks.isEmpty());
        return result;
    }

    @Override
    public KnowledgeRetrieveResult retrieve(List<Long> knowledgeIds, String query, int topK) {
        if (knowledgeIds == null || knowledgeIds.isEmpty()) {
            return retrieve((Long) null, query, topK);
        }
        if (knowledgeIds.size() == 1) {
            return retrieve(knowledgeIds.get(0), query, topK);
        }
        // 多个知识库时，从每个知识库检索后合并
        int perKbK = Math.max(1, topK / knowledgeIds.size());
        List<KnowledgeChunk> allChunks = new ArrayList<>();
        for (Long knowledgeId : knowledgeIds) {
            KnowledgeRetrieveResult singleResult = retrieve(knowledgeId, query, perKbK);
            if (singleResult.getChunks() != null) {
                allChunks.addAll(singleResult.getChunks());
            }
        }
        // 按分数排序并截取 topK
        allChunks.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        if (allChunks.size() > topK) {
            allChunks = allChunks.subList(0, topK);
        }
        String contextText = allChunks.stream()
                .map(KnowledgeChunk::getContent)
                .collect(Collectors.joining("\n\n"));

        KnowledgeRetrieveResult result = new KnowledgeRetrieveResult();
        result.setSuccess(true);
        result.setQuery(query);
        result.setRewriteQuery(query);
        result.setChunks(allChunks);
        result.setContextText(contextText);
        result.setTotalHit(allChunks.size());
        result.setHasResult(!allChunks.isEmpty());
        return result;
    }
}
