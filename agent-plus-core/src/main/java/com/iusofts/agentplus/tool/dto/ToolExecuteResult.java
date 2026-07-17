package com.iusofts.agentplus.tool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具执行结果.
 *
 * @author Ivan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolExecuteResult {

    /**
     * 是否成功.
     */
    private boolean success;

    /**
     * 结果数据.
     */
    private Object data;

    /**
     * 错误信息.
     */
    private String errorMessage;

    public static ToolExecuteResult success(Object data) {
        return ToolExecuteResult.builder()
            .success(true)
            .data(data)
            .build();
    }

    public static ToolExecuteResult error(String errorMessage) {
        return ToolExecuteResult.builder()
            .success(false)
            .errorMessage(errorMessage)
            .build();
    }
}
