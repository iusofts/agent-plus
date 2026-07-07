package com.iusofts.agentplus.aiflow.vo.workflow.data.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 参数 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@Schema(description = "参数")
public class Param {

    @NotBlank(message = "参数名称不能为空")
    @Schema(description = "参数名称")
    private String name;

    @NotBlank(message = "参数类型不能为空")
    @Schema(description = "参数类型")
    private String type;

    @Schema(description = "参数描述")
    private String description;

    @Schema(description = "默认值")
    private String defaultValue;

    @Schema(description = "是否必填")
    private Boolean required;

    @Schema(description = "是否默认参数")
    private Boolean isDefault;

    @Valid
    @Schema(description = "子参数列表")
    private List<Param> children;

}
