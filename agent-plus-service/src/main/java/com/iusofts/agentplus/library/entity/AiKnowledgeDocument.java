package com.iusofts.agentplus.library.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * AI知识库文档
 * </p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
@Getter
@Setter
@ToString
@TableName("ai_knowledge_document")
@Schema(name = "AiKnowledgeDocument", description = "AI知识库文档")
public class AiKnowledgeDocument implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "编号")
    @TableId(value = "id", type = IdType.NONE)
    private Long id;

    @Schema(description = "知识库ID")
    private Long knowledgeBaseId;

    @Schema(description = "文档名称")
    private String name;

    @Schema(description = "文档类型")
    private String docType;

    @Schema(description = "文档URL/路径")
    private String docUrl;

    @Schema(description = "文档内容摘要")
    private String summary;

    @Schema(description = "文档状态 0:待处理 1:处理中 2:已完成 3:失败")
    private Integer status;

    @Schema(description = "处理失败原因")
    private String errorMessage;

    @Schema(description = "分块数量")
    private Integer chunkCount;

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "最后更新人ID")
    private Long updateBy;

    @Schema(description = "记录最后更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "删除标记(0:正常 1:已删除)")
    private Boolean deleteFlag;

    @Schema(description = "所属组织ID")
    private Integer orgId;
}
