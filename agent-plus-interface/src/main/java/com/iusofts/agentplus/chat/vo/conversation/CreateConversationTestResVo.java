package com.iusofts.agentplus.chat.vo.conversation;

import com.iusofts.agentplus.chat.vo.AiMessageVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Ivan Shen
 */
@Data
public class CreateConversationTestResVo {

    @Schema(description = "会话id")
    private Long id;

    @Schema(description = "消息集合")
    private List<AiMessageVo> messages;

    public CreateConversationTestResVo() {
    }

    public CreateConversationTestResVo(Long id, List<AiMessageVo> messages) {
        this.id = id;
        this.messages = messages;
    }
}
