package com.iusofts.agentplus.chat.vo.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * ai智能体(对话流类型) 添加请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-21
 */
@Data
public class AiAgentAddChatReqVo {

    @NotBlank(message = "智能体名称不能为空")
    @Schema(description = "智能体名称")
    private String name;

    @Schema(description = "功能介绍")
    private String description;

    @Schema(description = "智能体图标")
    private String icon;

    @Schema(description = "设定描述")
    private String systemPrompt;

    @NotNull(message = "对话流不能为空")
    @Schema(description = "对话流ID")
    private Long chatFlowId;

    @Schema(description = "开场白文案")
    private String openingStatement;

    @Schema(description = "开场白预置问题")
    private List<String> openingQuestions;

    @Schema(description = "携带上下文轮数")
    private Integer contextRounds;

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
