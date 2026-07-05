package com.iusofts.agentplus.aiflow.vo.workflow.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 节点数据 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@Schema(description = "节点数据")
public class NodeData {

    @Schema(description = "标签")
    private String label;

    @Schema(description = "描述")
    private String description;

}
