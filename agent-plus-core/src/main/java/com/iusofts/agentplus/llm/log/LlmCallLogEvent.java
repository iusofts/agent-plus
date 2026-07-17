package com.iusofts.agentplus.llm.log;

import com.iusofts.agentplus.llm.dto.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * LLM 调用日志事件。
 *
 * @author Ivan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmCallLogEvent {

    /**
     * 链路追踪 ID。
     */
    private String traceId;

    /**
     * 调用来源：AGENT/CHAT/FLOW/API。
     */
    private String callSource;

    /**
     * 来源 ID：智能体 ID/会话 ID/流程 ID。
     */
    private Long sourceId;

    /**
     * 来源节点 ID：工作流节点 ID。
     */
    private String sourceNodeId;

    /**
     * 模型 ID。
     */
    private Long modelId;

    /**
     * 模型名称。
     */
    private String modelName;

    /**
     * 模型提供商。
     */
    private String modelProvider;

    /**
     * 温度参数。
     */
    private BigDecimal temperature;

    /**
     * 最大生成长度。
     */
    private Integer maxTokens;

    /**
     * 输入消息列表。
     */
    private List<ChatMessage> inputMessages;

    /**
     * 输入字符数。
     */
    private Integer inputCharCount;

    /**
     * 输入 Token 数。
     */
    private Integer inputTokens;

    /**
     * 输出内容。
     */
    private String outputContent;

    /**
     * 输出字符数。
     */
    private Integer outputCharCount;

    /**
     * 输出 Token 数。
     */
    private Integer outputTokens;

    /**
     * 总 Token 数。
     */
    private Integer totalTokens;

    /**
     * 是否成功。
     */
    private Boolean success;

    /**
     * 错误码。
     */
    private String errorCode;

    /**
     * 错误信息。
     */
    private String errorMessage;

    /**
     * 调用耗时（毫秒）。
     */
    private Integer durationMs;

    /**
     * 操作人 ID。
     */
    private Long operatorId;

    /**
     * 组织 ID。
     */
    private Integer orgId;
}
