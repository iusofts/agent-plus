package com.iusofts.agentplus.basics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IndustryVo {
    @Schema(description = "自增主键id")
    private Long id;

    @Schema(description = "行业名称")
    private String name;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态（1.启用 2.停用）")
    private Integer status;

    @Schema(description = "创建人")
    private Long createBy;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人")
    private Long updateBy;

    @Schema(description = "记录更新时间")
    private LocalDateTime updateTime;

}
