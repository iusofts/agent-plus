package com.iusofts.basic.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 状态数量
 * @author Ivan
 */
@Data
@Schema(description = "状态数量")
public class StateCount {

    @Schema(description = "状态")
    private Integer state;

    @Schema(description = "数量")
    private Integer count;

}
