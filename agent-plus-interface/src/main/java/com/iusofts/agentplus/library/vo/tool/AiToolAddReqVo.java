package com.iusofts.agentplus.library.vo.tool;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * <p>
 * ai工具 新增请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-12
 */
@Data
public class AiToolAddReqVo {

    @NotBlank(message = "工具名称不能为空")
    @Schema(description = "工具名称")
    private String name;

    @NotBlank(message = "工具编码不能为空")
    @Schema(description = "工具唯一编码")
    private String code;

    @NotNull(message = "工具类型不能为空")
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

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
