package com.iusofts.aiflow.vo.workflow.style;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 节点样式 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@Schema(description = "节点样式")
public class NodeStyle {

    @Schema(description = "高度")
    private String height;
    
    @Schema(description = "宽度")
    private String width;


}
