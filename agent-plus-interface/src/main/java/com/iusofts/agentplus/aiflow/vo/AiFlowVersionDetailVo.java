package com.iusofts.agentplus.aiflow.vo;

import com.iusofts.agentplus.aiflow.vo.workflow.Workflow;
import com.iusofts.agentplus.aiflow.vo.workflow.config.WorkflowConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * AI流程版本 详情对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
public class AiFlowVersionDetailVo {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "关联ai_flow主键")
    private Long flowId;

    @Schema(description = "语义化版本 v1.0.0")
    private String versionNo;

    @Schema(description = "版本别名/标题")
    private String versionName;

    @Schema(description = "修改备注")
    private String remark;

    @Schema(description = "VueFlow画布完整数据")
    private Workflow workflow;

    @Schema(description = "流程全局配置(入参、超时、重试、权限等)")
    private WorkflowConfig config;

    @Schema(description = "发布状态 0草稿 1已发布 2待审核")
    private Integer publishingStatus;

    @Schema(description = "发布时间")
    private LocalDateTime publishingTime;

    @Schema(description = "发布人ID")
    private Long publishingBy;

    @Schema(description = "版本生成时间")
    private LocalDateTime createTime;

}
