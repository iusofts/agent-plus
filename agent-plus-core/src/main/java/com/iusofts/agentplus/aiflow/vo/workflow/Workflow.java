package com.iusofts.agentplus.aiflow.vo.workflow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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

    @Valid
    @NotEmpty(message = "节点列表不能为空")
    @Schema(description = "节点列表")
    private List<Node> nodes;

    @Valid
    @Schema(description = "边列表")
    private List<Edge> edges;

    @Schema(description = "位置")
    private List<Double> position;

    @Schema(description = "缩放比例")
    private Double zoom;

    @Schema(description = "视口")
    private Viewport viewport;

}
