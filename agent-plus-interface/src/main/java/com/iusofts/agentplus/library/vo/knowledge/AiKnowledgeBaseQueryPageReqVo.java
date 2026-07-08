package com.iusofts.agentplus.library.vo.knowledge;

import com.iusofts.agentplus.basic.web.vo.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * AI知识库 查询分页请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiKnowledgeBaseQueryPageReqVo extends PageQuery {

    @Schema(description = "知识库名称")
    private String name;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

}
