package com.iusofts.agentplus.ailog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * LLM 调用日志明细。
 *
 * @author Ivan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "LLM 调用日志明细")
public class LlmCallLogItem {

    @Schema(description = "日志 ID")
    private Long id;

    @Schema(description = "链路追踪 ID")
    private String traceId;

    @Schema(description = "调用来源")
    private String callSource;

    @Schema(description = "来源 ID")
    private Long sourceId;

    @Schema(description = "来源流程 ID")
    private Long sourceFlowId;

    @Schema(description = "来源节点 ID")
    private String sourceNodeId;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "模型提供商")
    private String modelProvider;

    @Schema(description = "温度参数")
    private BigDecimal temperature;

    @Schema(description = "输入字符数")
    private Integer inputCharCount;

    @Schema(description = "输入 Token 数")
    private Integer inputTokens;

    @Schema(description = "输出字符数")
    private Integer outputCharCount;

    @Schema(description = "输出 Token 数")
    private Integer outputTokens;

    @Schema(description = "总 Token 数")
    private Integer totalTokens;

    @Schema(description = "结束原因(STOP/TOOL_EXECUTION等)")
    private String finishReason;

    @Schema(description = "下发给模型的工具规格列表")
    private List<com.iusofts.agentplus.llm.dto.ToolDefinition> toolDefinitions;

    @Schema(description = "模型请求的工具调用列表")
    private List<com.iusofts.agentplus.llm.dto.ToolCall> toolCalls;

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "耗时（毫秒）")
    private Integer durationMs;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
