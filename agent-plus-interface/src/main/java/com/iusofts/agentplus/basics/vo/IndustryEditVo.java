package com.iusofts.agentplus.basics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IndustryEditVo extends IndustryAddReqVo{
    @NotNull
    @Schema(description = "自增主键id")
    private Long id;
}
