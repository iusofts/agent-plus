package com.iusofts.agentplus.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * ai对话消息
 * </p>
 *
 * @author Ivan
 * @since 2026-05-08
 */
@Getter
@Setter
@ToString
@TableName("ai_call_log")
@Schema(name = "AiCallLog", description = "ai对话消息")
public class AiCallLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "业务类型")
    private Integer businessType;

    @Schema(description = "业务ID")
    private Long businessId;

    @Schema(description = "智能体ID")
    private Long agentId;

    @Schema(description = "智能体类型")
    private Integer agentType;

    @Schema(description = "输入消耗token数")
    private Integer inputTokens;

    @Schema(description = "输出消耗token数")
    private Integer outputTokens;

    @Schema(description = "总消耗token数")
    private Integer totalTokens;
    
    @Schema(description = "调用时长(ms)")
    private Integer duration;

    @Schema(description = "日期")
    private LocalDate timeSign;

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "最后更新人ID")
    private Long updateBy;

    @Schema(description = "记录最后更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "删除标记(0:正常 1:已删除)")
    private Integer deleteFlag;

    @Schema(description = "所属组织ID")
    private Integer orgId;
}
