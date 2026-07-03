package com.iusofts.aiflow.vo.workflow.style;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 标签背景样式 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@Schema(description = "标签背景样式")
public class LabelBgStyle {

    @Schema(description = "填充颜色")
    private String fill;

}
