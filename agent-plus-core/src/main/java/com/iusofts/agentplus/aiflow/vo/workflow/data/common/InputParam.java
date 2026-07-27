package com.iusofts.agentplus.aiflow.vo.workflow.data.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 * 输入参数 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@Schema(description = "输入参数")
public class InputParam {

    @NotBlank(message = "输入参数名称不能为空")
    @Schema(description = "参数名称")
    private String name;

    @NotBlank(message = "输入参数类型不能为空")
    @Schema(description = "参数类型")
    private String type;

    @Schema(description = "数组项类型")
    private String itemType;

    @Valid
    @NotNull(message = "输入参数映射键不能为空")
    @Schema(description = "参数映射键")
    private ParamMapKey paramMapKey;

}
