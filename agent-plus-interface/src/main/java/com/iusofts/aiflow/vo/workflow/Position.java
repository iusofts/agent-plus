package com.iusofts.aiflow.vo.workflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 位置 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@Schema(description = "位置")
public class Position {

    @Schema(description = "X坐标")
    private Double x;

    @Schema(description = "Y坐标")
    private Double y;

}
