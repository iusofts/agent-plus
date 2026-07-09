package com.iusofts.agentplus.library.vo.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 * AI知识库文档分块 编辑请求对象
 * </p>
 *
 * <p>编辑分块内容后需重新向量化并覆盖向量库中对应向量。</p>
 *
 * @author Ivan
 * @since 2026-07-09
 */
@Data
public class AiKnowledgeChunkEditReqVo {

    @NotNull(message = "编号不能为空")
    @Schema(description = "编号")
    private Long id;

    @NotBlank(message = "分块内容不能为空")
    @Schema(description = "分块内容")
    private String content;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
