package com.iusofts.basics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IndustryChangeStatusVo {
    @NotNull
    @Schema(description = "自增主键id")
    private Long id;

    @NotNull
    @Schema(description = "状态（1.启用 2.停用）")
    private Integer status;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;
}
