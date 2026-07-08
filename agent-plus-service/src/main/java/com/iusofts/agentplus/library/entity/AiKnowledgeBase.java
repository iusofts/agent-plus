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
 * AI知识库
 * </p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
@Getter
@Setter
@ToString
@TableName("ai_knowledge_base")
@Schema(name = "AiKnowledgeBase", description = "AI知识库")
public class AiKnowledgeBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "编号")
    @TableId(value = "id", type = IdType.NONE)
    private Long id;

    @Schema(description = "知识库名称")
    private String name;

    @Schema(description = "知识库描述")
    private String description;

    @Schema(description = "向量库集合名称")
    private String collectionName;

    @Schema(description = "嵌入模型ID")
    private Long embeddingModelId;

    @Schema(description = "分块大小")
    private Integer chunkSize;

    @Schema(description = "分块重叠大小")
    private Integer chunkOverlap;

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
