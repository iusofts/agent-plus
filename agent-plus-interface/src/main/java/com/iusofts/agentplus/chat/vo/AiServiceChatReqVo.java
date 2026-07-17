package com.iusofts.agentplus.chat.vo;

import com.iusofts.agentplus.basic.file.FileDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Ivan Shen
 */
@Data
public class AiServiceChatReqVo {

    @Schema(description = "会话id（不传表示创建新会话）")
    private Long conversationId;

    @Schema(description = "本轮用户输入内容")
    private String content;

    @Schema(description = "文件")
    private List<FileDto> fileList;

    /**
     * 会话id不为空时以下参数可免传
     */

    @Schema(description = "智能体ID")
    private Long agentId;

    @Schema(description = "组织ID")
    private Integer orgId;

    @Schema(description = "操作人ID")
    private Long operatorId;

}
