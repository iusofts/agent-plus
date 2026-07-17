package com.iusofts.agentplus.aiflow.vo.workflow.data;

import com.iusofts.agentplus.aiflow.vo.workflow.data.common.InputParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * <p>
 * 含输入参数的节点数据基类。
 * </p>
 *
 * <p>LLM、知识库、工具、批处理等节点均带有输入参数列表,统一继承此类,
 * 便于按 {@code instanceof} 判断节点是否支持基于输入参数的处理(如单节点试运行)。</p>
 *
 * @author Ivan
 * @since 2026-07-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "含输入参数的节点数据基类")
public class InputParamNodeData extends NodeData {

    @Valid
    @Schema(description = "输入参数列表")
    private List<InputParam> inputParams;

}
