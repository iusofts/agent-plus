package com.iusofts.ai.vo.conversation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * ai对话会话 更新标题请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-03-31
 */
@Data
public class AiConversationUpdateTitleReqVo {

    @Schema(description = "会话id")
    private Long id;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}