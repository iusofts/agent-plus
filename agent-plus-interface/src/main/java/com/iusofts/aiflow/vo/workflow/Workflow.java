package com.iusofts.aiflow.vo.workflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 工作流 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@Schema(description = "工作流")
public class Workflow {

    @Schema(description = "节点列表")
    private List<Node> nodes;

    @Schema(description = "边列表")
    private List<Edge> edges;

    @Schema(description = "位置")
    private List<Double> position;

    @Schema(description = "缩放比例")
    private Double zoom;

    @Schema(description = "视口")
    private Viewport viewport;

}
