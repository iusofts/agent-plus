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
import java.time.LocalDateTime;

/**
 * <p>
 * ai对话会话
 * </p>
 *
 * @author Ivan
 * @since 2026-03-31
 */
@Getter
@Setter
@ToString
@TableName("ai_conversation")
@Schema(name = "AiConversation", description = "ai对话会话")
public class AiConversation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会话id")
    @TableId(value = "id", type = IdType.NONE)
    private Long id;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "业务类型 0.测试 1.默认应用  ")
    private Integer businessType;

    @Schema(description = "业务id")
    private String businessId;

    @Schema(description = "智能体id")
    private Long agentId;

    @Schema(description = "智能体类型 1.问候型 2.销售型 3.鉴别型")
    private Integer agentType;

    @Schema(description = "使用模型")
    private String model;

    @Schema(description = "当前轮次")
    private Integer currentRounds;

    @Schema(description = "最后聊天时间")
    private LocalDateTime lastChatTime;

    @Schema(description = "测试数据")
    private String testData;

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
