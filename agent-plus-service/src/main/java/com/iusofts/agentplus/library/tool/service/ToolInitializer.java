package com.iusofts.agentplus.library.tool.service;

import com.iusofts.agentplus.library.tool.entity.AiTool;
import com.iusofts.agentplus.library.tool.mapper.AiToolMapper;
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

    @Resource
    private AiToolMapper aiToolMapper;

    @Resource
    private List<Tool> builtInTools;

    @PostConstruct
    public void init() {
        LOGGER.info("开始初始化内置工具，共 {} 个", builtInTools.size());
        for (Tool tool : builtInTools) {
            syncBuiltInTool(tool);
        }
        LOGGER.info("内置工具初始化完成");
    }

    private void syncBuiltInTool(Tool tool) {
        AiTool existing = aiToolMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiTool>()
                        .eq(AiTool::getCode, tool.getCode())
        );

        if (existing == null) {
            AiTool aiTool = new AiTool();
            aiTool.setCode(tool.getCode());
            aiTool.setName(tool.getName());
            aiTool.setDescription(tool.getDescription());
            aiTool.setType(1);
            aiTool.setStatus(1);
            aiTool.setOrgId(1);
            aiToolMapper.insert(aiTool);
            LOGGER.info("新增内置工具: {}", tool.getCode());
        } else {
            if (!tool.getName().equals(existing.getName())
                    || !tool.getDescription().equals(existing.getDescription())) {
                existing.setName(tool.getName());
                existing.setDescription(tool.getDescription());
                aiToolMapper.updateById(existing);
                LOGGER.info("更新内置工具: {}", tool.getCode());
            }
        }
    }
}
