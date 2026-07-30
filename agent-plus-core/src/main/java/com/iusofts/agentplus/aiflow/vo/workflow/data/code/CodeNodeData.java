package com.iusofts.agentplus.aiflow.vo.workflow.data.code;

import com.iusofts.agentplus.aiflow.vo.workflow.data.InputParamNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 代码节点数据。
 *
 * @author Ivan
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "代码节点数据")
public class CodeNodeData extends InputParamNodeData {

    @NotBlank(message = "脚本类型不能为空")
    @Schema(description = "脚本类型: javascript", example = "javascript")
    private String scriptType;

    @NotBlank(message = "脚本内容不能为空")
    @Schema(description = "脚本内容")
    private String script;

    @Valid
    @Schema(description = "输出参数列表")
    private List<OutputParam> outputParams;

    @Schema(description = "超时时间(毫秒)", example = "30000")
    private Long timeout;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "错误处理方式")
    private String errorHandling;

    @Schema(description = "自定义错误内容")
    private String customErrorContent;

}
