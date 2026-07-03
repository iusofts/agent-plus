package com.iusofts.ai.vo.conversation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * ai对话会话 添加请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-03-31
 */
@Data
public class AiConversationAddReqVo {

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

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}