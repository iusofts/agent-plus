package com.iusofts.agentplus.ai.vo.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Ivan Shen
 */
@Data
public class AiServiceCallReqVo {

    @Schema(description = "发送的消息")
    private List<Message> messages;

    @Schema(description = "业务类型")
    private Integer businessType;

    @Schema(description = "业务ID")
    private Long businessID;

    @Schema(description = "智能体ID")
    private Long agentId;

    @Schema(description = "智能体类型")
    private Integer agentType;

    @Schema(description = "组织ID")
    private Integer orgId;

    @Schema(description = "操作人ID")
    private Long operatorId;

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
