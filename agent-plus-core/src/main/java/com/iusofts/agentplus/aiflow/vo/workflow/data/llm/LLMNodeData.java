package com.iusofts.agentplus.aiflow.vo.workflow.data.llm;

import com.iusofts.agentplus.aiflow.vo.workflow.data.InputParamNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * <p>
 * LLM节点数据 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "LLM节点数据")
public class LLMNodeData extends InputParamNodeData {

    @NotNull(message = "模型不能为空")
    @Schema(description = "模型ID")
    private Long modelId;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "温度参数")
    private Double temperature;

    @NotBlank(message = "系统提示词不能为空")
    @Schema(description = "系统提示词")
    private String systemPrompt;

    @NotBlank(message = "用户提示词不能为空")
    @Schema(description = "用户提示词")
    private String userPrompt;

    @Valid
    @Schema(description = "输出参数列表")
    private List<OutputParam> outputParams;

    @Schema(description = "超时时间(毫秒)", example = "30000")
    private Long timeout;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "错误处理方式")
    private String errorHandling;

    @Schema(description = "自定义错误内容")
    private String customErrorContent;

    @Schema(description = "是否开启会话历史")
    private Boolean enableHistory;

    @Schema(description = "携带上下文轮数")
    private Integer contextRounds;

    @Schema(description = "插件工具ID集合")
    private List<Long> toolIds;

}
