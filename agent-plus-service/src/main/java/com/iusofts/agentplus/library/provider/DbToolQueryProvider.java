package com.iusofts.agentplus.library.provider;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iusofts.agentplus.library.tool.entity.AiTool;
import com.iusofts.agentplus.library.tool.mapper.AiToolMapper;
import com.iusofts.agentplus.tool.ToolQueryProvider;
import com.iusofts.agentplus.tool.dto.HttpConfig;
import com.iusofts.agentplus.tool.dto.ToolDTO;
import com.iusofts.agentplus.tool.dto.ToolParam;
import com.iusofts.agentplus.tool.dto.ToolResponseParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据库工具查询实现（唯一允许注入 Mapper）。
 *
 * @author Ivan
 */
@Component
public class DbToolQueryProvider implements ToolQueryProvider {

    private final AiToolMapper toolMapper;
    private final ObjectMapper objectMapper;

    public DbToolQueryProvider(AiToolMapper toolMapper) {
        this.toolMapper = toolMapper;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public ToolDTO getByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        LambdaQueryWrapper<AiTool> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiTool::getCode, code);
        AiTool tool = toolMapper.selectOne(wrapper);
        if (tool == null) {
            return null;
        }

        return convertToDTO(tool);
    }

    @Override
    public ToolDTO getById(Long id) {
        if (id == null) {
            return null;
        }

        AiTool tool = toolMapper.selectById(id);
        if (tool == null) {
            return null;
        }

        return convertToDTO(tool);
    }

    @Override
    public List<ToolDTO> listEnabledTools() {
        LambdaQueryWrapper<AiTool> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiTool::getStatus, 1);
        List<AiTool> tools = toolMapper.selectList(wrapper);

        return tools.stream()
                .map(this::convertToDTO)
                .toList();
    }

    private ToolDTO convertToDTO(AiTool tool) {
        ToolDTO dto = new ToolDTO();
        dto.setId(tool.getId());
        dto.setName(tool.getName());
        dto.setCode(tool.getCode());
        dto.setType(tool.getType());
        dto.setDescription(tool.getDescription());
        dto.setIcon(tool.getIcon());
        dto.setStatus(tool.getStatus());

        if (tool.getParamsSchema() != null) {
            dto.setParamsSchema(convertToList(tool.getParamsSchema(), ToolParam.class));
        }
        if (tool.getResponseSchema() != null) {
            dto.setResponseSchema(convertToList(tool.getResponseSchema(), ToolResponseParam.class));
        }
        if (tool.getHttpConfig() != null) {
            dto.setHttpConfig(objectMapper.convertValue(tool.getHttpConfig(), HttpConfig.class));
        }

        return dto;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> convertToList(Object value, Class<T> clazz) {
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            return list.stream()
                    .map(item -> objectMapper.convertValue(item, clazz))
                    .toList();
        }
        return null;
    }

}
