package com.iusofts.agentplus.aiflow.vo.workflow.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 模型 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@Schema(description = "模型")
public class Model {

    @Schema(description = "模型ID")
    private Long id;

    @Schema(description = "模型名称")
    private String modelName;

}
