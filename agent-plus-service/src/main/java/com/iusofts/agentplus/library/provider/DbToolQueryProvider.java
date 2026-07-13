package com.iusofts.agentplus.library.provider;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iusofts.agentplus.library.entity.AiTool;
import com.iusofts.agentplus.library.mapper.AiToolMapper;
import com.iusofts.agentplus.library.vo.tool.AiToolHttpConfigVo;
import com.iusofts.agentplus.tool.ToolQueryProvider;
import com.iusofts.agentplus.tool.dto.HttpConfig;
import com.iusofts.agentplus.tool.dto.ToolDTO;
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

    public DbToolQueryProvider(AiToolMapper toolMapper) {
        this.toolMapper = toolMapper;
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
        dto.setParamsSchema(tool.getParamsSchema());
        dto.setResponseSchema(tool.getResponseSchema());

        AiToolHttpConfigVo httpConfigVo = tool.getHttpConfig();
        if (httpConfigVo != null) {
            HttpConfig httpConfig = new HttpConfig();
            httpConfig.setUrl("");
            httpConfig.setMethod("");
            httpConfig.setHeaders(null);
            httpConfig.setTimeout(0);
            
            dto.setHttpConfig(httpConfig);
        }

        return dto;
    }

}
