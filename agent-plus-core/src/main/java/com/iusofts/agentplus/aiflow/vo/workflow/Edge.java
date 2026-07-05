package com.iusofts.agentplus.aiflow.vo.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iusofts.agentplus.aiflow.vo.workflow.style.EdgeStyle;
import com.iusofts.agentplus.aiflow.vo.workflow.style.LabelBgStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 边 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@Schema(description = "边")
public class Edge {

    @Schema(description = "边ID")
    private String id;

    @Schema(description = "源节点ID")
    private String source;

    @Schema(description = "目标节点ID")
    private String target;

    @Schema(description = "源连接点")
    private String sourceHandle;

    @Schema(description = "目标连接点")
    private String targetHandle;

    @Schema(description = "自定义数据")
    private Object data;

    @Schema(description = "是否动画")
    private Boolean animated;

    @Schema(description = "边类型")
    private String type;

    @Schema(description = "结束标记")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String markerEnd;

    @Schema(description = "标签")
    private String label;

    @Schema(description = "边样式")
    private EdgeStyle style;

    @Schema(description = "标签背景样式")
    private LabelBgStyle labelBgStyle;

    @Schema(description = "源X坐标")
    private Double sourceX;

    @Schema(description = "源Y坐标")
    private Double sourceY;

    @Schema(description = "目标X坐标")
    private Double targetX;

    @Schema(description = "目标Y坐标")
    private Double targetY;

}
