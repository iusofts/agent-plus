package com.iusofts.agentplus.tool;

import com.iusofts.agentplus.tool.dto.ToolDTO;

import java.util.List;

/**
 * 工具查询 Provider.
 *
 * @author Ivan
 */
public interface ToolQueryProvider {

    /**
     * 根据工具编码获取工具.
     *
     * @param code 工具编码
     * @return 工具 DTO
     */
    ToolDTO getByCode(String code);

    /**
     * 根据工具 ID 获取工具.
     *
     * @param id 工具 ID
     * @return 工具 DTO
     */
    ToolDTO getById(Long id);

    /**
     * 获取所有启用的工具.
     *
     * @return 工具列表
     */
    List<ToolDTO> listEnabledTools();
}
