package com.iusofts.agentplus.library.vo.tool;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>
 * ai工具 详情数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-12
 */
@Data
public class AiToolDetailVo {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "工具名称")
    private String name;

    @Schema(description = "工具唯一编码")
    private String code;

    @Schema(description = "工具类型 1:内置工具 2:自定义工具")
    private Integer type;

    @Schema(description = "工具描述")
    private String description;

    @Schema(description = "图标地址")
    private String icon;

    @Schema(description = "参数定义(JSON Schema格式)")
    private Map<String, Object> paramsSchema;

    @Schema(description = "工具配置(JSON格式)")
    private Map<String, Object> config;

    @Schema(description = "启用状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "记录最后更新时间")
    private LocalDateTime updateTime;

}
