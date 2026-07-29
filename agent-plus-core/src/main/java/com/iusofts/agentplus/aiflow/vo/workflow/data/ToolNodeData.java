package com.iusofts.agentplus.aiflow.vo.workflow.data;

import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import com.iusofts.agentplus.aiflow.vo.workflow.data.tool.ToolInfo;
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

    @Schema(description = "工具图标")
    private String toolIcon;
    
    @Schema(description = "工具信息")
    private ToolInfo toolInfo;

    @Valid
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
