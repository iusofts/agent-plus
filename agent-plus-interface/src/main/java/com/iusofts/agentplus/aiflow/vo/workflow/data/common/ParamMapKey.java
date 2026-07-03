package com.iusofts.agentplus.aiflow.vo.workflow.data.common;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "来源节点ID")
    private String nodeId;

    @Schema(description = "来源参数名称")
    private String name;

    @Schema(description = "来源参数类型")
    private String type;

}
