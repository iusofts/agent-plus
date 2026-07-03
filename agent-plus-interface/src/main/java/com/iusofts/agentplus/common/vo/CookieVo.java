/*
 * Copyright (C) 2019 All rights reserved
 * Author: Ivan Shen
 * Date: 2025/5/20
 * Description:CookieVo.java
 */
package com.iusofts.agentplus.common.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Ivan Shen
 */
@NoArgsConstructor
@Data
public class CookieVo {

    @JsonProperty("name")
    private String name;
    @JsonProperty("value")
    private String value;
    @JsonProperty("domain")
    private String domain;
    @JsonProperty("path")
    private String path;
    @JsonProperty("expires")
    private Double expires;
    @JsonProperty("httpOnly")
    private Boolean httpOnly;
    @JsonProperty("secure")
    private Boolean secure;
    @JsonProperty("sameSite")
    private String sameSite;
    
}
