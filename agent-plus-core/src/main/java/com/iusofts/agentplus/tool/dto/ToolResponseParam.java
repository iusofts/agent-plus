package com.iusofts.agentplus.tool.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 工具响应参数定义.
 *
 * @author Ivan
 */
@Data
@Schema(description = "工具响应参数定义")
public class ToolResponseParam {

    @NotBlank(message = "参数名称不能为空")
    @Schema(description = "参数名称")
    private String name;

    @Schema(description = "参数描述")
    private String description;

    @NotBlank(message = "参数类型不能为空")
    @Schema(description = "参数类型", allowableValues = {
            "String", "File", "Image", "Doc", "Code", "PPT", "TXT",
            "Excel", "Audio", "Zip", "Video", "Integer", "Number",
            "Object", "Array", "Boolean"
    })
    private String type;

    @Schema(description = "是否开启")
    private Boolean enabled;

    @Valid
    @Schema(description = "子参数列表")
    private List<ToolResponseParam> children;

}
