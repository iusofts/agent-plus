package com.iusofts.agentplus.library.vo.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 * AI知识库文档分块 状态变更请求对象
 * </p>
 *
 * <p>启用会用 DB 中保存的 content 重新向量化写回向量库;停用会删除该分块的向量,
 * 使其不再被 RAG 检索命中。</p>
 *
 * @author Ivan
 * @since 2026-07-09
 */
@Data
public class AiKnowledgeChunkStatusReqVo {

    @NotNull(message = "编号不能为空")
    @Schema(description = "编号")
    private Long id;

    @NotNull(message = "目标状态不能为空")
    @Schema(description = "目标状态 0:停用 1:启用")
    private Integer status;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
