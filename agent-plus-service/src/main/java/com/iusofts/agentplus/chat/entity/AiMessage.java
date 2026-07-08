package com.iusofts.agentplus.chat.entity;

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
 * ai对话消息
 * </p>
 *
 * @author Ivan
 * @since 2026-03-31
 */
@Getter
@Setter
@ToString
@TableName("ai_message")
@Schema(name = "AiMessage", description = "ai对话消息")
public class AiMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "消息id")
    @TableId(value = "id", type = IdType.NONE)
    private Long id;

    @Schema(description = "会话id")
    private Long conversationId;

    @Schema(description = "角色(user/assistant)")
    private String role;

    @Schema(description = "消息内容")
    private String content;
    
    @Schema(description = "结构返回值")
    private String structRes;

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
    private Boolean deleteFlag;

    @Schema(description = "所属组织ID")
    private Integer orgId;
}
