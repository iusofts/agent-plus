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
 * 输出节点数据 数据传输对象
 * </p>
 *
 * <p>用于工作流中间过程的消息输出,执行后流程继续向下流转,不会结束工作流。
 * 输出内容固定为 {@code answerContent} 模板渲染后的文本,支持 {@code {{变量名}}} 占位符。</p>
 *
 * @author Ivan
 * @since 2026-08-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "输出节点数据")
public class OutputNodeData extends NodeData {

    @Valid
    @NotEmpty(message = "输出参数不能为空")
    @Schema(description = "输出参数列表")
    private List<OutputParam> outputParams;

    @NotBlank(message = "输出内容不能为空")
    @Schema(description = "输出内容（支持 {{node.name}} / {{name}} 模板）")
    private String answerContent = "";

    @Schema(description = "是否开启流式输出")
    private Boolean streamOutput = false;

}
