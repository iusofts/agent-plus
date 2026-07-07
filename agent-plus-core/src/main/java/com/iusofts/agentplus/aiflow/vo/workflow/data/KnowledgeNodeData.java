package com.iusofts.agentplus.aiflow.vo.workflow.data;

import com.iusofts.agentplus.aiflow.vo.workflow.data.common.InputParam;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;

import java.util.List;

/**
 * <p>
 * 知识库节点数据 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "知识库节点数据")
public class KnowledgeNodeData extends NodeData {

    @NotNull(message = "知识库不能为空")
    @Schema(description = "知识库ID")
    private Long knowledgeId;

    @Range(min = 1, max = 99, message = "返回结果数量不合法")
    @NotNull(message = "返回结果数量不能为空")
    @Schema(description = "返回结果数量")
    private Integer topK;

    @Valid
    @NotEmpty(message = "知识库节点输入参数列表不能为空")
    @Schema(description = "输入参数列表")
    private List<InputParam> inputParams;

    @Valid
    @Schema(description = "输出参数列表")
    private List<OutputParam> outputParams;

}
