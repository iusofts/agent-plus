package com.iusofts.agentplus.aiflow.vo.workflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 视口 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@Schema(description = "视口")
public class Viewport {

    @Schema(description = "X坐标")
    private Double x;

    @Schema(description = "Y坐标")
    private Double y;

    @Schema(description = "缩放比例")
    private Double zoom;

}
