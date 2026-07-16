package com.iusofts.agentplus.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * <p>
 * 单节点试运行 结果对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-16
 */
@Data
@Schema(description = "试运行节点结果")
public class AiFlowTrialNodeResultVo {

    @Schema(description = "节点ID")
    private String nodeId;

    @Schema(description = "节点类型")
    private String nodeType;

    @Schema(description = "运行状态 0未执行 1执行中 2成功 3失败 4跳过")
    private Integer runStatus;

    @Schema(description = "节点输出")
    private Map<String, Object> output;

    @Schema(description = "节点耗时(毫秒)")
    private Long costMs;

    @Schema(description = "异常堆栈信息")
    private String errorStack;

}
