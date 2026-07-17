package com.iusofts.agentplus.library.provider;

import com.iusofts.agentplus.library.entity.AiPlugin;
import com.iusofts.agentplus.library.entity.AiTool;
import com.iusofts.agentplus.library.mapper.AiPluginMapper;
import com.iusofts.agentplus.library.mapper.AiToolMapper;
import com.iusofts.agentplus.library.vo.tool.AiToolHttpConfigVo;
import com.iusofts.agentplus.plugin.dto.PluginConfig;
import com.iusofts.agentplus.tool.ToolQueryProvider;
import com.iusofts.agentplus.tool.dto.HttpConfig;
import com.iusofts.agentplus.tool.dto.ToolDTO;
import org.springframework.stereotype.Component;

/**
 * 数据库工具查询实现（唯一允许注入 Mapper）。
 *
 * @author Ivan
 */
@Component
public class DbToolQueryProvider implements ToolQueryProvider {

    private final AiToolMapper toolMapper;
    private final AiPluginMapper pluginMapper;

    public DbToolQueryProvider(AiToolMapper toolMapper, AiPluginMapper pluginMapper) {
        this.toolMapper = toolMapper;
        this.pluginMapper = pluginMapper;
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

    private ToolDTO convertToDTO(AiTool tool) {
        ToolDTO dto = new ToolDTO();
        dto.setId(tool.getId());
        dto.setName(tool.getName());
        dto.setType(tool.getType());
        dto.setDescription(tool.getDescription());
        dto.setIcon(tool.getIcon());
        dto.setStatus(tool.getStatus());
        dto.setParamsSchema(tool.getParamsSchema());
        dto.setResponseSchema(tool.getResponseSchema());

        dto.setHttpConfig(buildHttpConfig(tool));

        return dto;
    }

    /**
     * 结合插件配置与工具配置构建完整的 HTTP 配置。
     *
     * <p>插件提供请求地址(base url)与请求头，工具提供接口路径(uri)与请求方法。</p>
     */
    private HttpConfig buildHttpConfig(AiTool tool) {
        AiToolHttpConfigVo toolConfig = tool.getHttpConfig();
        if (toolConfig == null) {
            return null;
        }

        PluginConfig pluginConfig = null;
        if (tool.getPluginId() != null) {
            AiPlugin plugin = pluginMapper.selectById(tool.getPluginId());
            if (plugin != null) {
                pluginConfig = plugin.getPluginConfig();
            }
        }

        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setUrl(buildUrl(pluginConfig, toolConfig));
        httpConfig.setMethod(toolConfig.getMethod());
        httpConfig.setHeaders(pluginConfig != null ? pluginConfig.getHeaders() : null);

        return httpConfig;
    }

    /**
     * 拼接插件请求地址与工具接口路径。
     */
    private String buildUrl(PluginConfig pluginConfig, AiToolHttpConfigVo toolConfig) {
        String base = pluginConfig != null && pluginConfig.getUrl() != null ? pluginConfig.getUrl().trim() : "";
        String uri = toolConfig.getUri() != null ? toolConfig.getUri().trim() : "";

        if (base.isEmpty()) {
            return uri;
        }
        if (uri.isEmpty()) {
            return base;
        }

        String normalizedBase = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String normalizedUri = uri.startsWith("/") ? uri : "/" + uri;
        return normalizedBase + normalizedUri;
    }

}
