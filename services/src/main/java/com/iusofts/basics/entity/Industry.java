package com.iusofts.basics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "t_industry",autoResultMap = true)
@Schema(description="行业管理")
public class Industry extends Model<Industry> {
    private static final long serialVersionUID=1L;

    @Schema(description = "自增主键id")
    @TableId(value = "id", type = IdType.AUTO)
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

    @Schema(description = "软删除标记（0：未删除；1：已删除）")
    @TableLogic
    private Integer deleteFlag;

}
