package com.iusofts.agentplus.ai.vo.conversation;

import com.iusofts.agentplus.ai.vo.service.AiMessageVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "ai对话会话信息vo")
public class AiConversationTestInfoVo {

    @Schema(description = "会话id")
    private Long id;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "消息集合")
    private List<AiMessageVo> messages;

    @Schema(description = "会话用量")
    private Usage usage;

    @Data
    public static class Usage {
        @Schema(description = "输入消耗token数")
        private Integer inputTokens;

        @Schema(description = "输出消耗token数")
        private Integer outputTokens;

        @Schema(description = "总消耗token数")
        private Integer totalTokens;
    }
    
}
