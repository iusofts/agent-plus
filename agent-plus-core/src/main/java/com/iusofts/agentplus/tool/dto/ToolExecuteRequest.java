package com.iusofts.agentplus.tool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 工具执行请求.
 *
 * @author Ivan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolExecuteRequest {

    /**
     * 工具 ID.
     */
    private Long toolId;

    /**
     * 工具参数.
     */
    private Map<String, Object> params;
}
