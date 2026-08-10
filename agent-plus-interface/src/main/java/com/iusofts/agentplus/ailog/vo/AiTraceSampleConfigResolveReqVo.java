package com.iusofts.agentplus.ailog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * AI Trace 采样率预览请求。
 *
 * <p>按 用户级 > 组织级 > 全局级 > yml 兜底 解析实际命中的采样率,
 * 命中记录若 status=0(禁用)则视同未配置,继续向下找。</p>
 *
 * @author Ivan
 * @since 2026-08-10
 */
@Data
@Schema(description = "AI Trace 采样率预览请求")
public class AiTraceSampleConfigResolveReqVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID,可为空")
    private Long userId;

    @Schema(description = "组织ID,可为空")
    private Long orgId;
}
