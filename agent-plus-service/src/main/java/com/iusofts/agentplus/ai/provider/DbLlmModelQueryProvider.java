package com.iusofts.agentplus.ai.provider;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iusofts.agentplus.ai.entity.AiModel;
import com.iusofts.agentplus.ai.mapper.AiModelMapper;
import com.iusofts.agentplus.llm.LlmModelDTO;
import com.iusofts.agentplus.llm.LlmModelQueryProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 数据库 LLM 模型查询实现（唯一允许注入 Mapper）。
 *
 * @author Ivan
 */
@Component
public class DbLlmModelQueryProvider implements LlmModelQueryProvider {

    private final AiModelMapper aiModelMapper;

    public DbLlmModelQueryProvider(AiModelMapper aiModelMapper) {
        this.aiModelMapper = aiModelMapper;
    }

    @Override
    public LlmModelDTO getModel(Long modelId) {
        if (modelId == null) {
            throw new IllegalStateException("modelId 为空");
        }

        AiModel model = aiModelMapper.selectById(modelId);
        if (model == null) {
            throw new IllegalStateException("找不到模型配置: aiModel.id=" + modelId);
        }
        if (model.getStatus() != null && model.getStatus() == 0) {
            throw new IllegalStateException("模型已禁用: aiModel.id=" + modelId
                    + " (" + model.getDisplayName() + ")");
        }
        if (!StringUtils.hasText(model.getApiKey())) {
            throw new IllegalStateException("模型缺少 apiKey: aiModel.id=" + modelId);
        }
        if (!StringUtils.hasText(model.getModelName())) {
            throw new IllegalStateException("模型缺少 modelName: aiModel.id=" + modelId);
        }

        LlmModelDTO dto = new LlmModelDTO();
        dto.setId(model.getId());
        dto.setProvider(model.getProvider());
        dto.setApiKey(model.getApiKey());
        dto.setBaseUrl(model.getBaseUrl());
        dto.setModelName(model.getModelName());
        return dto;
    }

    @Override
    public Long getDefaultModelId() {
        LambdaQueryWrapper<AiModel> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiModel::getModelType, 1) // 1:LLM
                .eq(AiModel::getStatus, 1) // 1:启用
                .eq(AiModel::getIsDefault, 1); // 1:默认
        AiModel model = aiModelMapper.selectOne(wrapper);
        if (model == null) {
            throw new IllegalStateException("未配置默认 LLM 模型");
        }
        return model.getId();
    }
}
