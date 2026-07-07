package com.iusofts.agentplus.aiflow.vo.workflow.data.condition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 条件 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@Schema(description = "条件")
public class Condition {

    @NotBlank(message = "条件ID不能为空")
    @Schema(description = "条件ID")
    private String id;

    @NotBlank(message = "条件逻辑关系不能为空")
    @Schema(description = "逻辑关系(and/or)")
    private String logic;

    @Valid
    @NotEmpty(message = "条件规则列表不能为空")
    @Schema(description = "规则列表")
    private List<ConditionRule> rules;

}
