package com.iusofts.agentplus.aiflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 运行节点明细 详情对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
public class AiFlowRuntimeNodeDetailVo {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "关联运行实例ID")
    private Long runtimeId;

    @Schema(description = "VueFlow节点唯一id")
    private String nodeId;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "节点类型(Start/LLM/Knowledge/Condition/End)")
    private String nodeType;

    @Schema(description = "0未执行 1执行中 2成功 3失败 4跳过")
    private Integer runStatus;

    @Schema(description = "节点入参JSON")
    private String nodeInput;

    @Schema(description = "节点输出JSON")
    private String nodeOutput;

    @Schema(description = "异常堆栈信息")
    private String errorStack;

    @Schema(description = "节点开始时间")
    private LocalDateTime startTime;

    @Schema(description = "节点结束时间")
    private LocalDateTime endTime;

    @Schema(description = "节点耗时(毫秒)")
    private Long costMs;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
