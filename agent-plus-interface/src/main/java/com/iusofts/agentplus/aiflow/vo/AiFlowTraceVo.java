package com.iusofts.agentplus.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 流程运行时序火焰图 (Chrome Trace Event Format)
 * </p>
 *
 * @author Ivan
 */
@Data
public class AiFlowTraceVo {

    @Schema(description = "事件列表")
    private List<AiFlowTraceEventVo> traceEvents = new ArrayList<>();

    @Schema(description = "展示时间单位")
    private String displayTimeUnit = "ms";

}
