package com.iusofts.agentplus.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * <p>
 * 流程试运行 请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-16
 */
@Data
@Schema(description = "流程试运行请求")
public class AiFlowTrialRunFlowReqVo {

    @Schema(description = "版本ID(优先),为空时取流程最新版本")
    private Long versionId;

    @Schema(description = "流程ID,versionId 为空时按此取最新版本")
    private Long flowId;

    @Schema(description = "全局入参,键为入参名")
    private Map<String, Object> inputs;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
