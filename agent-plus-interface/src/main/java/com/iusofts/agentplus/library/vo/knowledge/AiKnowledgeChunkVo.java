package com.iusofts.agentplus.library.vo.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * AI知识库文档分块 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-09
 */
@Data
public class AiKnowledgeChunkVo {

    @Schema(description = "编号")
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

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "记录最后更新时间")
    private LocalDateTime updateTime;

}
