package com.iusofts.agentplus.library.provider;

import com.iusofts.agentplus.library.entity.AiModel;
import com.iusofts.agentplus.library.mapper.AiModelMapper;
import com.iusofts.agentplus.knowledge.dto.EmbeddingModelDTO;
import com.iusofts.agentplus.knowledge.EmbeddingModelQueryProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 数据库嵌入模型查询实现（唯一允许注入 Mapper）。
 *
 * @author Ivan
 */
@Component
public class DbEmbeddingModelQueryProvider implements EmbeddingModelQueryProvider {

    private final AiModelMapper aiModelMapper;

    public DbEmbeddingModelQueryProvider(AiModelMapper aiModelMapper) {
        this.aiModelMapper = aiModelMapper;
    }

    @Override
    public EmbeddingModelDTO getModel(Long modelId) {
        if (modelId == null) {
            throw new IllegalStateException("知识库未配置嵌入模型 (embeddingModelId 为空)");
        }

        AiModel model = aiModelMapper.selectById(modelId);
        if (model == null) {
            throw new IllegalStateException("找不到嵌入模型配置: aiModel.id=" + modelId);
        }
        if (model.getStatus() != null && model.getStatus() == 0) {
            throw new IllegalStateException("嵌入模型已禁用: aiModel.id=" + modelId
                    + " (" + model.getDisplayName() + ")");
        }
        if (model.getModelType() != null && model.getModelType() != 2) {
            throw new IllegalStateException("aiModel.id=" + modelId + " 非 Embedding 类型模型");
        }
        if (!StringUtils.hasText(model.getApiKey())) {
            throw new IllegalStateException("嵌入模型缺少 apiKey: aiModel.id=" + modelId);
        }
        if (!StringUtils.hasText(model.getModelName())) {
            throw new IllegalStateException("嵌入模型缺少 modelName: aiModel.id=" + modelId);
        }

        EmbeddingModelDTO dto = new EmbeddingModelDTO();
        dto.setId(model.getId());
        dto.setProvider(model.getProvider());
        dto.setApiKey(model.getApiKey());
        dto.setBaseUrl(model.getBaseUrl());
        dto.setModelName(model.getModelName());
        return dto;
    }
}
