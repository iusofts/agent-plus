package com.iusofts.agentplus.aiflow.vo.workflow.data.llm;

import com.iusofts.agentplus.aiflow.vo.workflow.data.NodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.InputParam;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class LLMNodeData extends NodeData {

    @Schema(description = "模型ID")
    private Long model;

    @Schema(description = "温度参数")
    private Double temperature;

    @Schema(description = "系统提示词")
    private String systemPrompt;

    @Schema(description = "输入参数列表")
    private List<InputParam> inputParams;

    @Schema(description = "输出参数列表")
    private List<OutputParam> outputParams;

    @Schema(description = "超时时间(秒)")
    private Integer timeout;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "错误处理方式")
    private String errorHandling;

    @Schema(description = "自定义错误内容")
    private String customErrorContent;

}
