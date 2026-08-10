package com.iusofts.agentplus.ailog.vo;

import com.iusofts.agentplus.basic.web.vo.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * AI Trace 采样率配置分页查询请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-08-10
 */
@Data
@Schema(description = "AI Trace 采样率配置分页查询请求")
public class AiTraceSampleConfigPageReqVo extends PageQuery {

    @Schema(description = "配置类型 1:全局 2:组织 3:用户")
    private Integer configType;

    @Schema(description = "目标ID(全局=0;组织=orgId;用户=userId)")
    private Long targetId;

    @Schema(description = "启用状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "备注(模糊匹配)")
    private String remark;
}
