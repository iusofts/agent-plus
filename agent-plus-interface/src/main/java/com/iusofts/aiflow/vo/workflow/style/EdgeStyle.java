package com.iusofts.aiflow.vo.workflow.style;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 边样式 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@Schema(description = "边样式")
public class EdgeStyle {

    @Schema(description = "描边颜色")
    private String stroke;

    @Schema(description = "描边宽度")
    private Integer strokeWidth;

}
