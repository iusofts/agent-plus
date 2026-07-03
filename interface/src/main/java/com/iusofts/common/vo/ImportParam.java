package com.iusofts.common.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "批量导入参数")
public class ImportParam {

    @Schema(description = "文件名")
    private String fileName;
    
    @NotBlank(message = "文件不能为空")
    @Schema(description = "文件地址")
    private String fileUrl;

    @Schema(description = "所属组织ID")
    private Integer orgId;

    @Schema(description = "创建人id", hidden = true)
    private Long createrId;

    @Schema(description = "创建人姓名", hidden = true)
    private String createrName;

}
