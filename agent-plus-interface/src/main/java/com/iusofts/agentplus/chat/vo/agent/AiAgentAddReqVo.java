package com.iusofts.agentplus.chat.vo.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * ai智能体 添加请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-03-31
 */
@Data
public class AiAgentAddReqVo {

    @NotBlank(message = "智能体名称不能为空")
    @Schema(description = "智能体名称")
    private String name;

    @Schema(description = "类型 1:自主规划 2:对话流")
    private Integer type;

    @Schema(description = "功能介绍")
    private String description;

    @Schema(description = "智能体图标")
    private String icon;

    @Schema(description = "设定描述")
    private String systemPrompt;

    @Schema(description = "使用模型ID")
    private Long modelId;

    @Schema(description = "绑定工作流ID列表")
    private List<Long> workflowIds;

    @Schema(description = "绑定工具ID列表")
    private List<Long> toolIds;

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

    @Schema(description = "知识库召回条数")
    private Integer retrievalTopK;

    @Schema(description = "是否启用用户问题建议 0:否 1:是")
    private Integer enableQuestionSuggestion;

    @Schema(description = "是否自定义建议提示词 0:否 1:是")
    private Integer customSuggestionPrompt;

    @Schema(description = "自定义建议提示词")
    private String suggestionPrompt;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;
    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
