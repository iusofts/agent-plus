package com.iusofts.agentplus.chat.vo.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * ai智能体 详情数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-03-31
 */
@Data
public class AiAgentDetailVo {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "智能体名称")
    private String name;

    @Schema(description = "设定描述")
    private String systemPrompt;

    @Schema(description = "使用模型ID")
    private Long modelId;

    @Schema(description = "绑定工作流ID列表")
    private List<Long> workflowIds;

    @Schema(description = "开场白文案")
    private String openingStatement;

    @Schema(description = "开场白预置问题")
    private List<String> openingQuestions;

    @Schema(description = "绑定知识库ID列表")
    private List<Long> knowledgeBaseIds;

    @Schema(description = "生成随机性(temperature)")
    private BigDecimal temperature;

    @Schema(description = "携带上下文轮数")
    private Integer contextRounds;

    @Schema(description = "最大回复长度")
    private Integer maxReplyLength;

    @Schema(description = "最大推理回答长度")
    private Integer maxInferenceLength;

    @Schema(description = "是否启用用户问题建议 0:否 1:是")
    private Integer enableQuestionSuggestion;

    @Schema(description = "是否自定义建议提示词 0:否 1:是")
    private Integer customSuggestionPrompt;

    @Schema(description = "自定义建议提示词")
    private String suggestionPrompt;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "记录最后更新时间")
    private LocalDateTime updateTime;

}
