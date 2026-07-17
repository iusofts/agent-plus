package com.iusofts.agentplus.aiflow.vo.workflow.data.condition;

import com.iusofts.agentplus.aiflow.vo.workflow.data.NodeData;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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

    @Valid
    @NotEmpty(message = "条件不能为空")
    @Schema(description = "条件列表")
    private List<Condition> conditions;

}
