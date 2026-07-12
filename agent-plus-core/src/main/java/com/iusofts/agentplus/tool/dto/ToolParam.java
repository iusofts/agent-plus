package com.iusofts.agentplus.tool.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 工具参数定义.
 *
 * @author Ivan
 */
@Data
@Schema(description = "工具参数定义")
public class ToolParam {

    @NotBlank(message = "参数名称不能为空")
    @Schema(description = "参数名称")
    private String name;

    @NotBlank(message = "参数描述不能为空")
    @Schema(description = "参数描述")
    private String description;

    @NotBlank(message = "参数类型不能为空")
    @Schema(description = "参数类型", allowableValues = {
            "String", "File", "Image", "Doc", "Code", "PPT", "TXT",
            "Excel", "Audio", "Zip", "Video", "Integer", "Number",
            "Object", "Array", "Boolean"
    })
    private String type;

    @Schema(description = "传入方法(仅http工具需要)")
    private String injectMethod;

    @Schema(description = "是否必填")
    private Boolean required;

    @Schema(description = "默认值")
    private String defaultValue;

    @Schema(description = "是否开启")
    private Boolean enabled;

    @Valid
    @Schema(description = "子参数列表")
    private List<ToolParam> children;

}
