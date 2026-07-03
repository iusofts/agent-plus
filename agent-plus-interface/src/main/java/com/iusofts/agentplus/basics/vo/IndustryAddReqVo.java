package com.iusofts.agentplus.basics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class IndustryAddReqVo {
    @NotBlank
    @Schema(description = "行业名称")
    private String name;

    @NotNull
    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;
}
