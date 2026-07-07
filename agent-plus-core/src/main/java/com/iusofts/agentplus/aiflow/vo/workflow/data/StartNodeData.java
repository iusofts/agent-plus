package com.iusofts.agentplus.aiflow.vo.workflow.data;

import com.iusofts.agentplus.aiflow.vo.workflow.data.common.Param;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * <p>
 * 开始节点数据 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "开始节点数据")
public class StartNodeData extends NodeData {

    @Valid
    @NotEmpty(message = "开始节点输入参数列表不能为空")
    @Schema(description = "输入参数列表")
    private List<Param> inputParams;

}
