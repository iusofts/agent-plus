package com.iusofts.agentplus.aiflow.vo.workflow.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 工作流配置 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@Schema(description = "工作流配置")
public class WorkflowConfig {

    @Schema(description = "环境变量列表")
    private List<EnvVar> envVars;

}
