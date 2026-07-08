package com.iusofts.agentplus.ai.vo.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * AI知识库 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
@Data
public class AiKnowledgeBaseVo {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "知识库名称")
    private String name;

    @Schema(description = "知识库描述")
    private String description;

    @Schema(description = "嵌入模型ID")
    private Long embeddingModelId;

    @Schema(description = "分块大小")
    private Integer chunkSize;

    @Schema(description = "分块重叠大小")
    private Integer chunkOverlap;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "记录最后更新时间")
    private LocalDateTime updateTime;

}
