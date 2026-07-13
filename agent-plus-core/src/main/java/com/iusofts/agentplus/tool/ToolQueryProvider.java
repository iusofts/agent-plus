package com.iusofts.agentplus.tool;

import com.iusofts.agentplus.tool.dto.ToolDTO;

/**
 * 工具查询 Provider.
 *
 * @author Ivan
 */
public interface ToolQueryProvider {

    /**
     * 根据工具 ID 获取工具.
     *
     * @param id 工具 ID
     * @return 工具 DTO
     */
    ToolDTO getById(Long id);
}
