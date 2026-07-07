package com.iusofts.agentplus.aiflow.vo.workflow.data.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * <p>
 * 输出参数 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@Schema(description = "输出参数")
public class OutputParam {

    @NotBlank(message = "输出参数名称不能为空")
    @Schema(description = "参数名称")
    private String name;

    @NotBlank(message = "输出参数类型不能为空")
    @Schema(description = "参数类型")
    private String type;

    @Schema(description = "参数描述")
    private String description;

    @Valid
    @Schema(description = "参数映射键")
    private ParamMapKey paramMapKey;

}
