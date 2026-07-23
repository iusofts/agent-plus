package com.iusofts.agentplus.aiflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 流程运行实例
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Getter
@Setter
@ToString
@TableName("ai_flow_runtime")
@Schema(name = "AiFlowRuntime", description = "流程运行实例")
public class AiFlowRuntime implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "执行实例ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "流程ID")
    private Long flowId;

    @Schema(description = "流程名称(冗余)")
    private String flowName;

    @Schema(description = "执行使用的语义化版本v1.0.0")
    private String versionNo;

    @Schema(description = "全局追踪ID")
    private String traceId;

    @Schema(description = "运行状态 0等待 1运行中 2成功 3失败 4终止")
    private Integer runStatus;

    @Schema(description = "是否试运行 0:正式 1:流程试运行 2:节点试运行")
    private Integer trialFlag;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "本次执行入参JSON")
    private String inputParams;

    @Schema(description = "全局输出结果")
    private String outputResult;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "耗时毫秒")
    private Long costMs;

    @Schema(description = "触发人")
    private Long createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新操作人")
    private Long updateBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "软删除标记")
    private Integer deleteFlag;

}
