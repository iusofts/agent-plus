package com.iusofts.agentplus.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AI知识库文档处理日志
 *
 * @author Ivan
 * @since 2026-07-09
 */
@Getter
@Setter
@ToString
@TableName("ai_knowledge_doc_log")
@Schema(name = "AiKnowledgeDocLog", description = "AI知识库文档处理日志")
public class AiKnowledgeDocLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "知识库ID")
    private Long knowledgeBaseId;

    @Schema(description = "知识库名称")
    private String knowledgeBaseName;

    @Schema(description = "文档ID")
    private Long docId;

    @Schema(description = "文档名称")
    private String docName;

    @Schema(description = "操作类型(ADD/UPDATE/DELETE)")
    private String operationType;

    @Schema(description = "分块数量")
    private Integer chunkCount;

    @Schema(description = "总字符数")
    private Integer totalCharCount;

    @Schema(description = "embedding总消耗token")
    private Integer totalEmbeddingTokens;

    @Schema(description = "调用状态(0:失败 1:成功)")
    private Integer callStatus;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "调用开始时间")
    private LocalDateTime startTime;

    @Schema(description = "调用结束时间")
    private LocalDateTime endTime;

    @Schema(description = "调用时长(毫秒)")
    private Integer duration;

    @Schema(description = "日期")
    private LocalDate timeSign;

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "所属组织ID")
    private Integer orgId;
}
