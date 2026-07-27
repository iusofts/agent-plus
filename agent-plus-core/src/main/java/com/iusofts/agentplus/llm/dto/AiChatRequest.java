package com.iusofts.agentplus.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 聊天请求参数封装。
 *
 * <p>把原先 {@code chat(modelId, messages, config, tools)} 的散参聚合成一个对象，
 * 便于后续扩展新参数而不破坏方法签名。</p>
 *
 * @author Ivan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {

    /** 模型 ID。 */
    private Long modelId;

    /** 消息列表。 */
    private List<AiChatMessage> messages;

    /** 模型配置（温度、重试等）。 */
    private LlmModelConfigDTO config;

    /** 工具定义列表（可为 null 表示无工具）。 */
    private List<ToolDefinition> tools;
}
