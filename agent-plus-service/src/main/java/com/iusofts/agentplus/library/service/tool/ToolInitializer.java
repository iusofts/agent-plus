package com.iusofts.agentplus.library.service.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iusofts.agentplus.library.entity.AiPlugin;
import com.iusofts.agentplus.library.entity.AiTool;
import com.iusofts.agentplus.library.mapper.AiPluginMapper;
import com.iusofts.agentplus.library.mapper.AiToolMapper;
import com.iusofts.agentplus.tool.Tool;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 工具初始化器，负责将内置工具同步到数据库
 *
 * @author Ivan
 */
@Component
public class ToolInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToolInitializer.class);

    /**
     * 内置插件ID.
     */
    private static final Long BUILT_IN_PLUGIN_ID = 800000000L;

    /**
     * 内置插件名称.
     */
    private static final String BUILT_IN_PLUGIN_NAME = "内置插件";

    @Resource
    private AiPluginMapper aiPluginMapper;

    @Resource
    private AiToolMapper aiToolMapper;

    @Resource
    private List<Tool> builtInTools;

    @PostConstruct
    public void init() {
        initBuiltInPlugin();
        LOGGER.info("开始初始化内置工具，共 {} 个", builtInTools.size());
        for (Tool tool : builtInTools) {
            syncBuiltInTool(tool);
        }
        LOGGER.info("内置工具初始化完成");
    }

    private void initBuiltInPlugin() {
        AiPlugin existing = aiPluginMapper.selectById(BUILT_IN_PLUGIN_ID);
        if (existing != null) {
            return;
        }

        AiPlugin plugin = new AiPlugin();
        plugin.setId(BUILT_IN_PLUGIN_ID);
        plugin.setName(BUILT_IN_PLUGIN_NAME);
        plugin.setPluginType(1);
        plugin.setDescription("系统内置工具插件，聚合平台自带的内置工具");
        plugin.setSort(0);
        plugin.setStatus(1);
        plugin.setOrgId(1);

        aiPluginMapper.insert(plugin);
        LOGGER.info("初始化内置插件: {}", BUILT_IN_PLUGIN_NAME);
    }

    private void syncBuiltInTool(Tool tool) {
        AiTool existing = aiToolMapper.selectOne(
                new LambdaQueryWrapper<AiTool>()
                        .eq(AiTool::getPluginId, BUILT_IN_PLUGIN_ID)
                        .eq(AiTool::getName, tool.getName())
        );

        if (existing == null) {
            AiTool aiTool = new AiTool();
            aiTool.setName(tool.getName());
            aiTool.setPluginId(BUILT_IN_PLUGIN_ID);
            aiTool.setDescription(tool.getDescription());
            aiTool.setType(1);
            aiTool.setStatus(1);
            aiTool.setOrgId(1);
            aiTool.setParamsSchema(tool.getInputParams());
            aiTool.setResponseSchema(tool.getOutputParams());

            aiToolMapper.insert(aiTool);
            LOGGER.info("新增内置工具: {}", tool.getName());
        } else {
            existing.setName(tool.getName());
            existing.setDescription(tool.getDescription());
            existing.setParamsSchema(tool.getInputParams());
            existing.setResponseSchema(tool.getOutputParams());
            aiToolMapper.updateById(existing);
            LOGGER.info("更新内置工具: {}", tool.getName());
        }
    }
}
