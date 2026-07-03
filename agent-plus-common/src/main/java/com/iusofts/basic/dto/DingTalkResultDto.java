/*
 * Copyright (C) 2019 All rights reserved
 * Author: Ivan Shen
 * Date: 2020/4/3
 * Description:StringDto.java
 */
package com.iusofts.basic.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 钉钉返回消息Dto
 *
 * @author Ivan Shen
 */
@Data
@Schema(description = "钉钉返回消息Dto")
public class DingTalkResultDto {

    @Schema(description = "字符串返回值")
    private Long errcode;

    @Schema(description = "字符串返回值")
    private String errmsg;

    @Schema(description = "字符串返回值")
    private Long taskId;

}
