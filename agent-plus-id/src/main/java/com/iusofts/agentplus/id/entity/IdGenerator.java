package com.iusofts.agentplus.id.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description =  "id生成器")
public class IdGenerator {

    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "当前id值")
    private Integer uid;

    @Schema(description = "最小步长")
    private Integer stepMin;

    @Schema(description = "最大步长")
    private Integer stepMax;

}