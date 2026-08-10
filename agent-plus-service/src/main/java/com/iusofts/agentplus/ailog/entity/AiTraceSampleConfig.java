package com.iusofts.agentplus.ailog.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI Trace 采样率配置。
 *
 * <p>支持按 {@code config_type} 区分作用域:
 * <ul>
 *   <li>1:全局 —— 整租户共享一份,target_id=0</li>
 *   <li>2:组织 —— 每个 orgId 一份</li>
 *   <li>3:用户 —— 每个 userId 一份</li>
 * </ul>
 * 解析优先级:用户 > 组织 > 全局 > yml 兜底(default-sample-rate)。</p>
 *
 * @author Ivan
 * @since 2026-08-10
 */
@Getter
@Setter
@ToString
@TableName("ai_trace_sample_config")
@Schema(name = "AiTraceSampleConfig", description = "AI Trace 采样率配置")
public class AiTraceSampleConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "配置类型 1:全局 2:组织 3:用户")
    private Integer configType;

    @Schema(description = "目标ID(全局=0;组织=orgId;用户=userId)")
    private Long targetId;

    /**
     * 目标名称(展示/搜索用,组织名/用户昵称/全局占位)。
     * <p>显式指定列名,避免下划线/驼峰转换在某些环境下的隐式差异;
     * {@link FieldStrategy#ALWAYS} 保证 INSERT/UPDATE 时即使值为 null 也会被写入 SQL,
     * 避免 MP 默认 NOT_NULL 策略吞字段。</p>
     */
    @Schema(description = "目标名称(展示/搜索用,组织名/用户昵称/全局占位)")
    @TableField(value = "target_name", insertStrategy = FieldStrategy.ALWAYS, updateStrategy = FieldStrategy.ALWAYS)
    private String targetName;

    @Schema(description = "采样率,取值 0.0000 ~ 1.0000")
    private BigDecimal sampleRate;

    @Schema(description = "启用状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人ID")
    private Long createBy;

    /**
     * 创建人姓名(展示用,来源于 sys_user.name 或 controller 注入)。
     * 显式列名 + ALWAYS 策略,见 {@link #targetName} 说明。
     */
    @Schema(description = "创建人姓名(展示用,来源于 sys_user.name)")
    @TableField(value = "create_by_name", insertStrategy = FieldStrategy.ALWAYS, updateStrategy = FieldStrategy.ALWAYS)
    private String createByName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "最后更新人ID")
    private Long updateBy;

    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "软删除 0:正常 1:已删除")
    private Integer deleteFlag;

    // ============ 业务常量 ============

    /** 配置类型 - 全局 */
    public static final int TYPE_GLOBAL = 1;
    /** 配置类型 - 组织 */
    public static final int TYPE_ORG = 2;
    /** 配置类型 - 用户 */
    public static final int TYPE_USER = 3;
    /** 全局配置的 target_id 占位值 */
    public static final long GLOBAL_TARGET_ID = 0L;
}
