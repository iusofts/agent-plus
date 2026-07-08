package com.iusofts.agentplus.basic.web.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "poi信息")
public class OptionDto {

    @Schema(description = "名称")
    private String label;

    @Schema(description = "值")
    private Integer value;

    @Schema(description = "子集")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<OptionDto> children;

}
