package com.iusofts.agentplus.aiflow.stream;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 节点产出完整内容事件。
 *
 * <p>节点(LLM / Output / End 等)执行完成后,把节点产出的<b>完整文本</b>一次性推给客户端,
 * 不再采用逐 token 增量流式(LLMTokenEvent 已弃用)。</p>
 *
 * <p>前端按 {@code nodeType} 区分:
 * <ul>
 *   <li>llm:大模型节点的完整输出</li>
 *   <li>output:输出节点的模板渲染结果(中间过程消息)</li>
 *   <li>end:结束节点的最终输出(isOutput=true,作为助手消息入库)</li>
 * </ul>
 * </p>
 *
 * @author Ivan
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class MessageCompleteEvent extends WorkflowStreamEvent {

    /** 节点 ID */
    private String nodeId;
    /** 节点类型(llm / output / end / ...) */
    private String nodeType;
    /** 节点名称 */
    private String nodeName;
    /** 节点产出的完整文本内容 */
    private String content;
    /** 是否为流程最终输出(End 节点或被作为最终消息的 Output 节点) */
    private Boolean isOutput;

    public static MessageCompleteEvent create(String runId,
                                              String nodeId,
                                              String nodeType,
                                              String nodeName,
                                              String content,
                                              Boolean isOutput) {
        MessageCompleteEvent event = new MessageCompleteEvent();
        event.setType(WorkflowStreamEventType.MESSAGE_COMPLETE.value());
        event.setRunId(runId);
        event.setNodeId(nodeId);
        event.setNodeType(nodeType);
        event.setNodeName(nodeName);
        event.setContent(content);
        event.setIsOutput(isOutput);
        event.setTimestamp(java.time.Instant.now().toEpochMilli());
        return event;
    }
}
