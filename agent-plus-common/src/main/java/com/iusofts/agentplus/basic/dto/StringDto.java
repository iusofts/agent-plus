/*
 * Copyright (C) 2019 All rights reserved
 * Author: Ivan Shen
 * Date: 2020/4/3
 * Description:StringDto.java
 */
package com.iusofts.agentplus.basic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 字符串Dto
 *
 * @author Ivan Shen
 */
@Data
public class StringDto {

    @Schema(description = "字符串返回值")
    private String res;

    public StringDto() {
    }

    public StringDto(String res) {
        this.res = res;
    }
}
