package com.iusofts.agentplus.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * ai智能体
 * </p>
 *
 * @author Ivan
 * @since 2026-03-31
 */
@Getter
@Setter
@ToString
@TableName(value = "ai_agent", autoResultMap = true)
@Schema(name = "AiAgent", description = "ai智能体")
public class AiAgent implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "编号")
    @TableId(value = "id", type = IdType.NONE)
    private Long id;

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

    @Schema(description = "绑定工作流ID列表(JSON数组存储)")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> workflowIds;

    @Schema(description = "绑定工具ID列表(JSON数组存储)")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> toolIds;

    @Schema(description = "开场白文案")
    private String openingStatement;

    @Schema(description = "开场白预置问题(JSON数组存储)")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> openingQuestions;

    @Schema(description = "绑定知识库ID列表(JSON数组存储)")
    @TableField(typeHandler = JacksonTypeHandler.class)
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

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "最后更新人ID")
    private Long updateBy;

    @Schema(description = "记录最后更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "删除标记(0:正常 1:已删除)")
    private Boolean deleteFlag;

    @Schema(description = "所属组织ID")
    private Integer orgId;

    @Schema(description = "启用状态 0:禁用 1:启用")
    private Integer status;

}
