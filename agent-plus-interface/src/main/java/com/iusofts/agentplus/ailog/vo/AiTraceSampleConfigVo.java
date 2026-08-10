package com.iusofts.agentplus.ailog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * AI Trace 采样率配置新增/修改请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-08-10
 */
@Data
@Schema(description = "AI Trace 采样率配置请求")
public class AiTraceSampleConfigVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID(修改时必填)")
    private Long id;

    @Schema(description = "配置类型 1:全局 2:组织 3:用户", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "配置类型不能为空")
    private Integer configType;

    @Schema(description = "目标ID(全局=0;组织=orgId;用户=userId),新建时可省略默认 0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long targetId;

    @Schema(description = "采样率,取值 0.0000 ~ 1.0000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "采样率不能为空")
    @DecimalMin(value = "0.0000", message = "采样率不能小于 0")
    @DecimalMax(value = "1.0000", message = "采样率不能大于 1")
    private BigDecimal sampleRate;

    @Schema(description = "启用状态 0:禁用 1:启用,默认 1")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    /**
     * 当前操作用户ID(由 controller 注入,service 落库 create_by / update_by 使用)。
     * 不参与接口展示,前端无需关心。
     */
    @Schema(hidden = true)
    private Long currentUserId;
}
