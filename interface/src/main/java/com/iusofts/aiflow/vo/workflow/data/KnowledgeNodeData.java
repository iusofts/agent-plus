package com.iusofts.aiflow.vo.workflow.data;

import com.iusofts.aiflow.vo.workflow.data.common.InputParam;
import com.iusofts.aiflow.vo.workflow.data.common.OutputParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

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

    @Schema(description = "知识库ID")
    private Long knowledgeId;

    @Schema(description = "返回结果数量")
    private Integer topK;

    @Schema(description = "输入参数列表")
    private List<InputParam> inputParams;

    @Schema(description = "输出参数列表")
    private List<OutputParam> outputParams;

}
