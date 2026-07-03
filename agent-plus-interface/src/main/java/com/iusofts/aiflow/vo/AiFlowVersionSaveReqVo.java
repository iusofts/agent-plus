package com.iusofts.aiflow.vo;

import com.iusofts.aiflow.vo.workflow.Workflow;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * AI流程版本 保存请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
public class AiFlowVersionSaveReqVo {

    @Schema(description = "主键ID（编辑时传入，新增时不传）")
    private Long id;

    @Schema(description = "关联ai_flow主键")
    private Long flowId;

    @Schema(description = "版本别名/标题")
    private String versionName;

    @Schema(description = "修改备注")
    private String remark;

    @Schema(description = "VueFlow画布完整数据")
    private Workflow workflow;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
