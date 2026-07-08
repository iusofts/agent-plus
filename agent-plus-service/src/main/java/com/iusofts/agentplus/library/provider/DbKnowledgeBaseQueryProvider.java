package com.iusofts.agentplus.library.provider;

import com.iusofts.agentplus.library.entity.AiKnowledgeBase;
import com.iusofts.agentplus.library.mapper.AiKnowledgeBaseMapper;
import com.iusofts.agentplus.knowledge.KnowledgeBaseDTO;
import com.iusofts.agentplus.knowledge.KnowledgeBaseQueryProvider;
import org.springframework.stereotype.Component;

/**
 * 数据库知识库查询实现（唯一允许注入 Mapper）。
 *
 * @author Ivan
 */
@Component
public class DbKnowledgeBaseQueryProvider implements KnowledgeBaseQueryProvider {

    private final AiKnowledgeBaseMapper knowledgeBaseMapper;

    public DbKnowledgeBaseQueryProvider(AiKnowledgeBaseMapper knowledgeBaseMapper) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
    }

    @Override
    public KnowledgeBaseDTO getKnowledgeBase(Long knowledgeBaseId) {
        if (knowledgeBaseId == null) {
            return null;
        }

        AiKnowledgeBase kb = knowledgeBaseMapper.selectById(knowledgeBaseId);
        if (kb == null) {
            return null;
        }

        KnowledgeBaseDTO dto = new KnowledgeBaseDTO();
        dto.setId(kb.getId());
        dto.setCollectionName(kb.getCollectionName());
        dto.setEmbeddingModelId(kb.getEmbeddingModelId());
        return dto;
    }
}
