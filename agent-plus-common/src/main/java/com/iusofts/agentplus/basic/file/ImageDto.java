/*
 * Copyright (C) 2006-2018 All rights reserved
 * Author: Ivan Shen
 * Date: 2019/8/11
 * Description:ImageDto.java
 */
package com.iusofts.agentplus.basic.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 图片数据
 *
 * @author Ivan Shen
 */
@Data
public class ImageDto {

    @Schema(description = "图片名称")
    private String name;

    @Schema(description = "图片地址")
    private String url;

    @Schema(description = "是否是首图 0：否")
    private int isFirst;

}
