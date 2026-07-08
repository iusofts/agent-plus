package com.iusofts.agentplus.ai.vo.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * AI知识库文档 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
@Data
public class AiKnowledgeDocumentVo {

    @Schema(description = "编号")
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

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "记录最后更新时间")
    private LocalDateTime updateTime;

}
