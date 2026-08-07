package com.iusofts.agentplus.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志大字段载荷附表。
 *
 * <p>与 {@link OperLog} 通过 {@code oper_log_id} 一对一关联，用于存储
 * 请求参数、返回参数、错误堆栈等大字段，使主表保持轻量，便于按天分表与查询。
 * 附表按主表同样的规则按天分表（表名 {@code sys_oper_log_payload_yyyy_MM_dd}）。</p>
 *
 * @author Ivan
 * @since 2026-08-07
 */
@Getter
@Setter
@ToString
@TableName("sys_oper_log_payload")
@Schema(name = "OperLogPayload", description = "操作日志大字段载荷")
public class OperLogPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "关联 sys_oper_log.id,唯一")
    private Long operLogId;

    @Schema(description = "请求参数(JSON)")
    private String operParam;

    @Schema(description = "返回参数(JSON)")
    private String jsonResult;

    @Schema(description = "错误消息(堆栈)")
    private String errorMsg;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;
}
