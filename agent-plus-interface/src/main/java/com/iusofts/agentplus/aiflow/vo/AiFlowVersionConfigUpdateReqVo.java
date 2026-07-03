package com.iusofts.agentplus.aiflow.vo;

import com.iusofts.agentplus.aiflow.vo.workflow.config.EnvVar;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * AI流程版本 配置更新请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
public class AiFlowVersionConfigUpdateReqVo {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "流程全局配置(入参、超时、重试、权限等)")
    private Config config;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

    
    @Data
    @Schema(description = "流程全局配置")
    public static class Config {

        @Schema(description = "环境变量列表")
        private List<EnvVar> envVars;
        
    }
    
}
