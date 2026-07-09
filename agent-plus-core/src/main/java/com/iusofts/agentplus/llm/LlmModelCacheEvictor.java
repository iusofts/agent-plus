package com.iusofts.agentplus.llm;

/**
 * LLM 模型缓存清理接口。
 *
 * <p>模型配置变更(编辑/删除)后调用，使已缓存的 ChatModel 失效，下次调用重建。</p>
 *
 * @author Ivan
 */
public interface LlmModelCacheEvictor {

    /**
     * 清理指定模型的所有缓存(含不同 temperature 的缓存条目)。
     *
     * @param modelId 模型 ID
     */
    void evict(Long modelId);
}
