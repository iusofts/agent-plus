package com.iusofts.agentplus.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

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
@TableName("ai_agent")
@Schema(name = "AiAgent", description = "ai智能体")
public class AiAgent implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "编号")
    @TableId(value = "id", type = IdType.NONE)
    private Long id;

    @Schema(description = "类型 1.问候型 2.销售型 3.鉴别型")
    private Integer type;

    @Schema(description = "智能体名称")
    private String name;
    
    @Schema(description = "代码")
    private String code;

    @Schema(description = "最大轮次")
    private Integer maxRounds;

    @Schema(description = "设定描述")
    private String systemPrompt;
    
    @Schema(description = "转人工")
    private String transferHuman;

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

    @Schema(description = "是否默认智能体 0:否 1:是")
    private Integer isDefault;

    @Schema(description = "是否系统预制 0:用户自定义 1:系统内置不可删改")
    private Integer isSystem;

    @Schema(description = "行业id")
    private Long industryId;

    @Schema(description = "行业名称")
    private String industryName;
}
