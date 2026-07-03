package com.iusofts.agentplus.ai.vo.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * AI评论筛选返回
 *
 * @author Codex
 */
@Data
public class AiCommentFilterResVo {

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "命中的智能体ID",hidden = true)
    private Long agentId;

    @Schema(description = "候选评论结构化列表")
    private List<CommentItem> commentIdList;

    @Schema(description = "渲染后的提示词，便于调试",hidden = true)
    private String renderedPrompt;

    @Schema(description = "模型原始回复，便于调试",hidden = true)
    private String rawReply;

    @Data
    public static class CommentItem {

        @Schema(description = "评论ID")
        private String commentId;

        @Schema(description = "所在城市，无法推测时为空字符串")
        private String city;

        @Schema(description = "意向车型，无法推测时为空字符串")
        private String model;

        @Schema(description = "贷款意向，无法推测时为空字符串")
        private String loans;

        @Schema(description = "购买意向等级（1 低  2 中  3高）")
        private Integer intentLevel;
    }
}
