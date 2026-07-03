package com.iusofts.agentplus.ai.vo.conversation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * ai对话会话 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-03-31
 */
@Data
public class AiConversationVo {

    @Schema(description = "会话id")
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

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "记录最后更新时间")
    private LocalDateTime updateTime;

}