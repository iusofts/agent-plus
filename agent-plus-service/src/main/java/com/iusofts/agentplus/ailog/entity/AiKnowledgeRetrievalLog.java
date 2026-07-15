package com.iusofts.agentplus.ailog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI知识库检索日志
 *
 * @author Ivan
 * @since 2026-07-09
 */
@Getter
@Setter
@ToString
@TableName(value = "ai_knowledge_retrieval_log", autoResultMap = true)
@Schema(name = "AiKnowledgeRetrievalLog", description = "AI知识库检索日志")
public class AiKnowledgeRetrievalLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "链路追踪ID")
    private String traceId;

    @Schema(description = "调用来源(AGENT/CHAT/FLOW/API)")
    private String callSource;

    @Schema(description = "来源ID(智能体ID/会话ID/流程ID)")
    private Long sourceId;

    @Schema(description = "知识库ID")
    private Long knowledgeBaseId;

    @Schema(description = "知识库名称")
    private String knowledgeBaseName;

    @Schema(description = "检索查询内容")
    private String query;

    @Schema(description = "查询字符数")
    private Integer queryCharCount;

    @Schema(description = "查询向量化消耗token")
    private Integer queryEmbeddingTokens;

    @Schema(description = "召回条数")
    private Integer topK;

    @Schema(description = "召回文档块列表(JSON)")
    @TableField(value = "retrieved_chunks", typeHandler = JacksonTypeHandler.class)
    private List<ChunkEntry> retrievedChunks;

    @Schema(description = "实际召回数量")
    private Integer retrievedCount;

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

    @Schema(description = "小时(0-23,用于按小时聚合)")
    private Integer hourSign;

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "所属组织ID")
    private Integer orgId;

    @Getter
    @Setter
    public static class ChunkEntry implements Serializable {
        private Long chunkId;
        private String content;
        private Double similarity;
    }
}
