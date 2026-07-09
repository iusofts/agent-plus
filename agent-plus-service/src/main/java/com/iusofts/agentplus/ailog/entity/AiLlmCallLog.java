package com.iusofts.agentplus.ailog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI大模型调用日志
 *
 * @author Ivan
 * @since 2026-07-09
 */
@Getter
@Setter
@ToString
@TableName(value = "ai_llm_call_log", autoResultMap = true)
@Schema(name = "AiLlmCallLog", description = "AI大模型调用日志")
public class AiLlmCallLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "链路追踪ID")
    private String traceId;

    @Schema(description = "调用来源(AGENT/CHAT/FLOW/API)")
    private String callSource;

    @Schema(description = "来源ID(智能体ID/会话ID/流程ID)")
    private Long sourceId;

    @Schema(description = "来源节点ID(工作流节点ID)")
    private String sourceNodeId;

    @Schema(description = "业务类型")
    private Integer businessType;

    @Schema(description = "业务ID")
    private Long businessId;

    @Schema(description = "模型ID")
    private Long modelId;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "模型提供商(QWEN/DOUBAO/OPENAI/CUSTOM)")
    private String modelProvider;

    @Schema(description = "生成温度")
    private BigDecimal temperature;

    @Schema(description = "最大生成长度")
    private Integer maxTokens;

    @Schema(description = "输入消息列表(JSON)")
    @TableField(value = "input_messages", typeHandler = JacksonTypeHandler.class)
    private List<MessageEntry> inputMessages;

    @Schema(description = "输入字符数")
    private Integer inputCharCount;

    @Schema(description = "输入消耗token数")
    private Integer inputTokens;

    @Schema(description = "输出内容")
    private String outputContent;

    @Schema(description = "输出字符数")
    private Integer outputCharCount;

    @Schema(description = "输出消耗token数")
    private Integer outputTokens;

    @Schema(description = "总消耗token数")
    private Integer totalTokens;

    @Schema(description = "调用状态(0:失败 1:成功)")
    private Integer callStatus;

    @Schema(description = "错误码")
    private String errorCode;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "调用开始时间")
    private LocalDateTime startTime;

    @Schema(description = "调用结束时间")
    private LocalDateTime endTime;

    @Schema(description = "调用时长(毫秒)")
    private Integer duration;

    @Schema(description = "日期")
    private LocalDate timeSign;

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "所属组织ID")
    private Integer orgId;

    @Getter
    @Setter
    public static class MessageEntry implements Serializable {
        private String role;
        private String content;
    }
}
