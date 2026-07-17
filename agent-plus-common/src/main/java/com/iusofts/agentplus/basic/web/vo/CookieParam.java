package com.iusofts.agentplus.basic.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author Ivan Shen
 */
@Schema(description = "Cookie参数")
public class CookieParam {

    @Schema(description = "参数名称")
    private String name;
    @Schema(description = "参数值")
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
