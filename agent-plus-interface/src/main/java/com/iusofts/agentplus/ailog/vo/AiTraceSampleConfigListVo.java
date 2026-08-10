package com.iusofts.agentplus.ailog.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.iusofts.agentplus.basic.web.vo.page.RawLongSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * AI Trace 采样率配置列表返回对象
 * </p>
 *
 * @author Ivan
 * @since 2026-08-10
 */
@Data
@Schema(description = "AI Trace 采样率配置列表项")
public class AiTraceSampleConfigListVo {

    @Schema(description = "主键ID")
    @JsonSerialize(using = RawLongSerializer.class)
    private Long id;

    @Schema(description = "配置类型 1:全局 2:组织 3:用户")
    private Integer configType;

    @Schema(description = "配置类型描述")
    private String configTypeDesc;

    @Schema(description = "目标ID(全局=0;组织=orgId;用户=userId)")
    @JsonSerialize(using = RawLongSerializer.class)
    private Long targetId;

    @Schema(description = "目标名称(展示/搜索用,组织名/用户昵称/全局占位)")
    private String targetName;

    @Schema(description = "采样率,取值 0.0000 ~ 1.0000")
    private BigDecimal sampleRate;

    @Schema(description = "启用状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建人姓名")
    private String createByName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "最后更新人")
    private String updateBy;

    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;
}
