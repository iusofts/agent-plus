package com.iusofts.agentplus.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * <p>
 * 单节点试运行 请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-16
 */
@Data
@Schema(description = "单节点试运行请求")
public class AiFlowTrialRunNodeReqVo {

    @Schema(description = "版本ID(优先),为空时取流程最新版本")
    private Long versionId;

    @Schema(description = "流程ID,versionId 为空时按此取最新版本")
    private Long flowId;

    @Schema(description = "目标节点ID")
    private String nodeId;

    @Schema(description = "全局入参,键为入参名")
    private Map<String, Object> inputs;

    @Schema(description = "上游节点模拟输出,外层键为节点ID,内层键为输出参数名")
    private Map<String, Map<String, Object>> upstreamOutputs;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
