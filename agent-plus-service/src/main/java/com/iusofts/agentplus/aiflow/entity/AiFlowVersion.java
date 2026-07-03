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
 * AI流程版本画布表
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Getter
@Setter
@ToString
@TableName("ai_flow_version")
@Schema(name = "AiFlowVersion", description = "AI流程版本画布表")
public class AiFlowVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "关联ai_flow主键")
    private Long flowId;

    @Schema(description = "语义化版本 v1.0.0")
    private String versionNo;

    @Schema(description = "版本别名/标题")
    private String versionName;

    @Schema(description = "修改备注")
    private String remark;

    @Schema(description = "VueFlow画布完整数据JSON(nodes/edges/viewport)")
    private String flowJson;

    @Schema(description = "流程全局配置(入参、超时、重试、权限等)")
    private String configJson;

    @Schema(description = "发布状态 0草稿 1已发布 2待审核")
    private Integer publishingStatus;

    @Schema(description = "发布时间")
    private LocalDateTime publishingTime;

    @Schema(description = "发布人ID")
    private Long publishingBy;

    @Schema(description = "保存操作人")
    private Long createBy;

    @Schema(description = "版本生成时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人")
    private Long updateBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "软删除")
    private Integer deleteFlag;

}
