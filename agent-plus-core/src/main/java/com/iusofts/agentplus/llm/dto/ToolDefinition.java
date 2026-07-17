package com.iusofts.agentplus.llm.dto;

import com.iusofts.agentplus.tool.dto.ToolParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 下发给模型的工具规格定义（function calling）。
 *
 * @author Ivan Shen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolDefinition {

    /**
     * 工具名称。
     */
    private String name;

    /**
     * 工具描述。
     */
    private String description;

    /**
     * 入参定义列表。
     */
    private List<ToolParam> parameters;
}
