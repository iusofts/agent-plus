package com.iusofts.agentplus.tool.dto;

import lombok.Data;

import java.util.List;

/**
 * 工具 DTO.
 *
 * @author Ivan
 */
@Data
public class ToolDTO {

    /**
     * 工具 ID.
     */
    private Long id;

    /**
     * 工具名称.
     */
    private String name;

    /**
     * 工具唯一编码.
     */
    private String code;

    /**
     * 工具类型 1:内置工具 2:服务接口 3:MCP.
     */
    private Integer type;

    /**
     * 工具描述.
     */
    private String description;

    /**
     * 图标地址.
     */
    private String icon;

    /**
     * 参数定义列表.
     */
    private List<ToolParam> paramsSchema;

    /**
     * 响应定义列表.
     */
    private List<ToolResponseParam> responseSchema;

    /**
     * HTTP配置.
     */
    private HttpConfig httpConfig;

    /**
     * 启用状态 0:禁用 1:启用.
     */
    private Integer status;

}
