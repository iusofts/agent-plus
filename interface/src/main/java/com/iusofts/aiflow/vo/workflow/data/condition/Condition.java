package com.iusofts.aiflow.vo.workflow.data.condition;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "条件ID")
    private String id;

    @Schema(description = "逻辑关系(and/or)")
    private String logic;

    @Schema(description = "规则列表")
    private List<ConditionRule> rules;

}
