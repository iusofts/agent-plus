package com.iusofts.agentplus.aiflow.vo.workflow.data.condition;

import com.iusofts.agentplus.aiflow.vo.workflow.data.common.ParamMapKey;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 * 条件规则 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@Schema(description = "条件规则")
public class ConditionRule {

    @NotBlank(message = "条件规则ID不能为空")
    @Schema(description = "规则ID")
    private String id;

    @Valid
    @NotNull(message = "条件规则变量不能为空")
    @Schema(description = "变量")
    private ParamMapKey variable;

    @NotBlank(message = "条件规则运算符不能为空")
    @Schema(description = "运算符")
    private String operator;

    @Schema(description = "值")
    private String value;

}
