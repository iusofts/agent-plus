package com.iusofts.agentplus.aiflow.vo.workflow.data;

import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 工具节点数据.
 *
 * @author Ivan
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工具节点数据")
public class ToolNodeData extends InputParamNodeData {

    @NotNull(message = "工具不能为空")
    @Schema(description = "工具ID")
    private Long toolId;

    @Schema(description = "工具编码（冗余，方便使用")
    private String toolCode;

    @Valid
    @Schema(description = "输出参数列表")
    private List<OutputParam> outputParams;
}
