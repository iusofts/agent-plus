package com.iusofts.agentplus.library.vo.knowledge;

import com.iusofts.agentplus.basic.web.vo.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * AI知识库文档 查询分页请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiKnowledgeDocumentQueryPageReqVo extends PageQuery {

    @NotNull(message = "知识库ID不能为空")
    @Schema(description = "知识库ID")
    private Long knowledgeBaseId;

    @Schema(description = "文档名称")
    private String name;

    @Schema(description = "文档状态 0:待处理 1:处理中 2:已完成 3:失败")
    private Integer status;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

}
