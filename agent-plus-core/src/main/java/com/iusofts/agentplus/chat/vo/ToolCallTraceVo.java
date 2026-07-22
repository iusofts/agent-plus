package com.iusofts.agentplus.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 工具调用轨迹（对话中 function calling 的一次工具执行记录，不落库，仅随本轮回答返回）。
 *
 * @author Ivan Shen
 */
@Data
@Schema(name = "ToolCallTraceVo", description = "工具调用轨迹")
public class ToolCallTraceVo {

    @Schema(description = "工具ID")
    private Long toolId;

    @Schema(description = "工具名称")
    private String toolName;

    @Schema(description = "调用参数")
    private Object arguments;

    @Schema(description = "是否执行成功")
    private boolean success;

    @Schema(description = "执行结果")
    private Object result;

    @Schema(description = "错误信息")
    private String errorMessage;
}
