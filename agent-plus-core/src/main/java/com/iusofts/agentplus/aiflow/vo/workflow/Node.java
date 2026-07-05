package com.iusofts.agentplus.aiflow.vo.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.iusofts.agentplus.aiflow.vo.workflow.data.*;
import com.iusofts.agentplus.aiflow.vo.workflow.data.condition.ConditionNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.data.llm.LLMNodeData;
import com.iusofts.agentplus.aiflow.vo.workflow.style.NodeStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 节点 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@Schema(description = "节点")
public class Node {

    @Schema(description = "节点ID")
    private String id;

    @Schema(description = "节点类型")
    private String type;

    @Schema(description = "是否已初始化")
    private Boolean initialized;

    @Schema(description = "位置")
    private Position position;

    @Schema(description = "标签")
    private String label;
    
    @Schema(description = "样式")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private NodeStyle style;

    @Schema(description = "节点数据")
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = StartNodeData.class, name = "Start"),
            @JsonSubTypes.Type(value = LLMNodeData.class, name = "LLM"),
            @JsonSubTypes.Type(value = KnowledgeNodeData.class, name = "Knowledge"),
            @JsonSubTypes.Type(value = ConditionNodeData.class, name = "Condition"),
            @JsonSubTypes.Type(value = BatchNodeData.class, name = "Batch"),
            @JsonSubTypes.Type(value = AggregatorNodeData.class, name = "Aggregator"),
            @JsonSubTypes.Type(value = EndNodeData.class, name = "End")
    })
    private NodeData data;

    @Schema(description = "父节点ID")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String parentNode;

    @Schema(description = "是否展开父节点")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean expandParent;

    @Schema(description = "范围")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String extent;

}
