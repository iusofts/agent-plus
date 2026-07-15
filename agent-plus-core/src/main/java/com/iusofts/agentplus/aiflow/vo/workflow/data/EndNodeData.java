package com.iusofts.agentplus.aiflow.vo.workflow.data;

import com.iusofts.agentplus.aiflow.vo.workflow.data.common.OutputParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * <p>
 * 结束节点数据 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "结束节点数据")
public class EndNodeData extends NodeData {

    @Valid
    @NotEmpty(message = "输出参数不能为空")
    @Schema(description = "输出参数列表")
    private List<OutputParam> outputParams;

    @NotBlank(message = "回答内容返回方式不能为空")
    @Schema(description = "回答内容返回方式：variable=返回变量 text=返回文本")
    private String answerMode = "text";

    @Schema(description = "返回文本模式下的内容（支持 {{变量名}} 模板）")
    private String answerContent = "";

    @Schema(description = "是否开启流式输出")
    private Boolean streamOutput = false;

}
