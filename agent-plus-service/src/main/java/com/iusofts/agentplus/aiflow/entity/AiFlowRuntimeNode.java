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
 * 运行节点明细
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Getter
@Setter
@ToString
@TableName("ai_flow_runtime_node")
@Schema(name = "AiFlowRuntimeNode", description = "运行节点明细")
public class AiFlowRuntimeNode implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "自增主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "关联运行实例ID")
    private Long runtimeId;

    @Schema(description = "VueFlow节点唯一id")
    private String nodeId;

    @Schema(description = "节点名称(冗余)")
    private String nodeName;

    @Schema(description = "节点类型(Start/LLM/Knowledge/Condition/End)")
    private String nodeType;

    @Schema(description = "0未执行 1执行中 2成功 3失败 4跳过")
    private Integer runStatus;

    @Schema(description = "异常堆栈信息")
    private String errorStack;

    @Schema(description = "节点开始时间")
    private LocalDateTime startTime;

    @Schema(description = "节点结束时间")
    private LocalDateTime endTime;

    @Schema(description = "节点耗时(毫秒)")
    private Long costMs;

    @Schema(description = "创建人")
    private Long createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人")
    private Long updateBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "软删除")
    private Integer deleteFlag;

}
