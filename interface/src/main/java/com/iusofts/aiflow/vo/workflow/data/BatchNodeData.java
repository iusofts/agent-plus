package com.iusofts.aiflow.vo.workflow.data;

import com.iusofts.aiflow.vo.workflow.data.common.InputParam;
import com.iusofts.aiflow.vo.workflow.data.common.OutputParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * <p>
 * 批处理节点数据 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "批处理节点数据")
public class BatchNodeData extends NodeData {

    @Schema(description = "最大并行数")
    private Integer maxParallel;

    @Schema(description = "输入参数列表")
    private List<InputParam> inputParams;

    @Schema(description = "输出参数列表")
    private List<OutputParam> outputParams;

}
