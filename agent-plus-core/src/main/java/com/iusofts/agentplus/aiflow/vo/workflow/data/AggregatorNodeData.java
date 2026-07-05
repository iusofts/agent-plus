package com.iusofts.agentplus.aiflow.vo.workflow.data;

import com.iusofts.agentplus.aiflow.vo.workflow.data.aggregator.OutputGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * <p>
 * 变量聚合节点数据 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "变量聚合节点数据")
public class AggregatorNodeData extends NodeData {

    @Schema(description = "输出分组列表")
    private List<OutputGroup> outputGroups;

}
