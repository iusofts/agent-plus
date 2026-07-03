package com.iusofts.ai.vo.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * AI评论筛选请求
 *
 * @author Codex
 */
@Data
public class AiCommentFilterReqVo {


    @Schema(description = "任务唯一标识ID")
    private Long id;

    @Schema(description = "所属组织ID")
    private Integer orgId;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;


    @Schema(description = "作品标题，与content二选一或至少传一个")
    private String title;

    @Schema(description = "作品内容，与title二选一或至少传一个")
    private String content;

    @Schema(description = "用户筛选评论意图")
    private String filterIntent;

    @Schema(description = "评论列表")
    private List<CommentItem> comments;

    @Data
    public static class CommentItem {

        @Schema(description = "评论内容")
        private String content;

        @Schema(description = "评论时间")
        private String commentTime;

        @Schema(description = "评论用户ID")
        private String userId;

        @Schema(description = "评论用户昵称")
        private String nickname;

        @Schema(description = "评论ID")
        private String commentId;
    }
}
