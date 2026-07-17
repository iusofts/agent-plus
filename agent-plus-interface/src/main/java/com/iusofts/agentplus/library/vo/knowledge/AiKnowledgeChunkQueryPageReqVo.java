package com.iusofts.agentplus.library.vo.knowledge;

import com.iusofts.agentplus.basic.web.vo.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * AI知识库文档分块 查询分页请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiKnowledgeChunkQueryPageReqVo extends PageQuery {

    @NotNull(message = "文档ID不能为空")
    @Schema(description = "文档ID")
    private Long documentId;

    @Schema(description = "分块内容关键字")
    private String keyword;

    @Schema(description = "分块状态 0:停用 1:启用")
    private Integer status;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

}
