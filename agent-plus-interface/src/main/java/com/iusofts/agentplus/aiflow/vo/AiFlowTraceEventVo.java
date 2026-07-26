package com.iusofts.agentplus.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 时序火焰图单个事件 (Chrome Trace Event Format)
 * </p>
 *
 * @author Ivan
 */
@Data
public class AiFlowTraceEventVo {

    @Schema(description = "span表主键ID")
    private Long id;

    @Schema(description = "节点展示名称，前端火焰图显示文本")
    private String name;

    @Schema(description = "事件类型，AI链路统一用X(Complete，起止区间)")
    private String ph;

    @Schema(description = "开始时间戳(微秒)")
    private Long ts;

    @Schema(description = "持续时长(微秒)")
    private Long dur;

    @Schema(description = "分类 workflow/workflow.node/knowledge/llm")
    private String cat;

}
