package com.iusofts.agentplus.library.vo.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 * AI知识库文档分块 手动新增请求对象
 * </p>
 *
 * <p>在指定文档下手动追加一个分块:后端生成 id 与 vectorId,sortOrder 取该文档当前最大序号+1,
 * 内容向量化后写入向量库,并使文档分块数 +1。</p>
 *
 * @author Ivan
 * @since 2026-07-09
 */
@Data
public class AiKnowledgeChunkAddReqVo {

    @NotNull(message = "文档ID不能为空")
    @Schema(description = "文档ID")
    private Long documentId;

    @NotBlank(message = "分块内容不能为空")
    @Schema(description = "分块内容")
    private String content;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
