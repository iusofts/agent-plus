package com.iusofts.aiflow.vo.workflow.data.condition;

import com.iusofts.aiflow.vo.workflow.data.NodeData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * <p>
 * 条件节点数据 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "条件节点数据")
public class ConditionNodeData extends NodeData {

    @Schema(description = "条件列表")
    private List<Condition> conditions;

}
