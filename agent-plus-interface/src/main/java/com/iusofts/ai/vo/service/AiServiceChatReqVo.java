package com.iusofts.ai.vo.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Ivan Shen
 */
@Data
public class AiServiceChatReqVo {

    @Schema(description = "会话id（不传表示创建新会话）")
    private Long conversationId;

    @Schema(description = "发送的消息")
    private List<Message> messages;

    /**
     * 会话id不为空时以下参数可免传
     */

    @Schema(description = "智能体ID")
    private Long agentId;

    @Schema(description = "业务类型")
    private Integer businessType;

    @Schema(description = "业务ID")
    private String businessID;

    @Schema(description = "默认智能体提示词 false时需要自己从messages中传入")
    private boolean defaultPrompt = true;

    @Schema(description = "组织ID")
    private Integer orgId;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Schema(description = "对话消息")
    public static class Message {

        @Schema(description = "角色(user/assistant/system)")
        private String role;

        @Schema(description = "消息内容")
        private String content;

    }
    
}
