package com.iusofts.agentplus.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 流程运行时序树形结构节点
 * </p>
 *
 * @author Ivan
 */
@Data
public class AiFlowTraceTreeVo {

    @Schema(description = "节点展示名称")
    private String label;

    @Schema(description = "持续时长(微秒)")
    private Long dur;

    @Schema(description = "分类，工作流为workflow，节点用节点类型(Start/LLM/Knowledge等)，节点子级继承节点类型，用于前端区分显示图标")
    private String cat;

    @Schema(description = "业务类型 runtime/runtimeNode/knowledgeLog/llmLog，对应数据所在表")
    private String businessType;

    @Schema(description = "业务ID，对应表的主键ID")
    private Long businessId;

    @Schema(description = "节点ID，业务类型为节点类型时有值")
    private String nodeId;

    @Schema(description = "子级")
    private List<AiFlowTraceTreeVo> children = new ArrayList<>();

}
