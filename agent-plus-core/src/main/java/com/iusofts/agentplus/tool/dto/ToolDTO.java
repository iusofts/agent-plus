package com.iusofts.agentplus.tool.dto;

import lombok.Data;

import java.util.Map;

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
     * 工具类型 1:内置工具 2:自定义工具.
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
     * 参数定义(JSON Schema格式).
     */
    private Map<String, Object> paramsSchema;

    /**
     * 工具配置(JSON格式).
     */
    private Map<String, Object> config;
}
