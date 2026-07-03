package com.iusofts.aiflow.vo.workflow.data.condition;

import com.iusofts.aiflow.vo.workflow.data.common.ParamMapKey;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "规则ID")
    private String id;

    @Schema(description = "变量")
    private ParamMapKey variable;

    @Schema(description = "运算符")
    private String operator;

    @Schema(description = "值")
    private String value;

}
