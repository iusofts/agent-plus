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
 * AI知识库文档分块
 * </p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
@Getter
@Setter
@ToString
@TableName("ai_knowledge_chunk")
@Schema(name = "AiKnowledgeChunk", description = "AI知识库文档分块")
public class AiKnowledgeChunk implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "编号")
    @TableId(value = "id", type = IdType.NONE)
    private Long id;

    @Schema(description = "知识库ID")
    private Long knowledgeBaseId;

    @Schema(description = "文档ID")
    private Long documentId;

    @Schema(description = "向量库中的ID")
    private String vectorId;

    @Schema(description = "分块内容")
    private String content;

    @Schema(description = "分块序号")
    private Integer sortOrder;

    @Schema(description = "分块状态 0:停用 1:启用")
    private Integer status;

    @Schema(description = "元数据JSON")
    private String metadata;

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
