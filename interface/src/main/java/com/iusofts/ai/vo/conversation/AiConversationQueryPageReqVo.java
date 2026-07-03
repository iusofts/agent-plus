package com.iusofts.ai.vo.conversation;

import com.iusofts.basic.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * ai对话会话 查询分页请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-03-31
 */
@Data
public class AiConversationQueryPageReqVo extends PageQuery {

    @Schema(description = "智能体id")
    private Long agentId;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

}