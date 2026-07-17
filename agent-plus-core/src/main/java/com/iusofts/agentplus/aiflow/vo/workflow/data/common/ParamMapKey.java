package com.iusofts.agentplus.aiflow.vo.workflow.data.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * <p>
 * 参数映射键 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@Schema(description = "参数映射键")
public class ParamMapKey {

    @NotBlank(message = "来源节点ID不能为空")
    @Schema(description = "来源节点ID")
    private String nodeId;

    @NotBlank(message = "来源参数名称不能为空")
    @Schema(description = "来源参数名称")
    private String name;

    @NotBlank(message = "来源参数类型不能为空")
    @Schema(description = "来源参数类型")
    private String type;

}
