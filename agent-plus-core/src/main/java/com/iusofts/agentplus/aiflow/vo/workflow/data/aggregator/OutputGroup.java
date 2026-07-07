package com.iusofts.agentplus.aiflow.vo.workflow.data.aggregator;

import com.iusofts.agentplus.aiflow.vo.workflow.data.common.ParamMapKey;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 输出分组 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@Schema(description = "输出分组")
public class OutputGroup {

    @NotBlank(message = "输出分组名称不能为空")
    @Schema(description = "分组名称")
    private String name;

    @NotBlank(message = "输出分组类型不能为空")
    @Schema(description = "分组类型")
    private String type;

    @Valid
    @NotEmpty(message = "聚合变量列表不能为空")
    @Schema(description = "聚合变量列表")
    private List<ParamMapKey> variables;

}
