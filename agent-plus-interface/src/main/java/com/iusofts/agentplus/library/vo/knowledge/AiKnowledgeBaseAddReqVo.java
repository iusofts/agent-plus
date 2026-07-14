package com.iusofts.agentplus.library.vo.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * <p>
 * AI知识库 添加请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
@Data
public class AiKnowledgeBaseAddReqVo {

    @NotBlank(message = "知识库名称不能为空")
    @Schema(description = "知识库名称")
    private String name;

    @Schema(description = "知识库描述")
    private String description;

    @Schema(description = "知识库图标")
    private String icon;

    @Schema(description = "嵌入模型ID")
    private Long embeddingModelId;

    @Schema(description = "分块大小")
    private Integer chunkSize;

    @Schema(description = "分块重叠大小")
    private Integer chunkOverlap;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
