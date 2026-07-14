package com.iusofts.agentplus.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型请求的一次工具调用。
 *
 * <p>由 LLM 在 function calling 时产生，业务侧据此执行对应工具并回填结果。</p>
 *
 * @author Ivan Shen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCall {

    /**
     * 工具调用 ID（用于回填结果时与请求对应）。
     */
    private String id;

    /**
     * 工具名称。
     */
    private String name;

    /**
     * 工具调用参数（JSON 字符串）。
     */
    private String arguments;
}
