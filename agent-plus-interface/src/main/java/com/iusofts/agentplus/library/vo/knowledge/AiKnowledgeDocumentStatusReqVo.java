package com.iusofts.agentplus.library.vo.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 * AI知识库文档 状态变更请求对象
 * </p>
 *
 * <p>仅支持在「可用」与「已禁用/已归档」之间切换。禁用/归档会联动停用文档下所有分块并
 * 删除其向量;恢复为可用会重新启用所有分块并重建向量。</p>
 *
 * @author Ivan
 * @since 2026-07-09
 */
@Data
public class AiKnowledgeDocumentStatusReqVo {

    @NotNull(message = "编号不能为空")
    @Schema(description = "编号")
    private Long id;

    @NotNull(message = "目标状态不能为空")
    @Schema(description = "目标状态 2:可用 4:已禁用 5:已归档")
    private Integer status;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
