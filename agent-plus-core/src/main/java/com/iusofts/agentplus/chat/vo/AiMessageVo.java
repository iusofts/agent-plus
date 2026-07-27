package com.iusofts.agentplus.chat.vo;

import com.iusofts.agentplus.basic.file.FileDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(name = "AiMessageVo", description = "ai对话消息vo")
public class AiMessageVo {

    @Schema(description = "消息id")
    private Long id;

    @Schema(description = "会话id")
    private Long conversationId;

    @Schema(description = "角色(user/assistant)")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "文件列表")
    private List<FileDto> fileList;

    @Schema(description = "结构返回值")
    private String structRes;

    @Schema(description = "智能体ID")
    private Long agentId;

    @Schema(description = "智能体类型")
    private Integer agentType;

    @Schema(description = "输入消耗token数")
    private Integer inputTokens;

    @Schema(description = "输出消耗token数")
    private Integer outputTokens;

    @Schema(description = "总消耗token数")
    private Integer totalTokens;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "需要转人工")
    private boolean needTransferHuman;

    @Schema(description = "本轮工具调用轨迹(不落库)")
    private List<ToolCallTraceVo> toolCalls;

}
