package com.iusofts.basic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 名称值对VO - 用于统计展示
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "NameValueVo", description = "名称值对")
public class NameValueVo {
    @Schema(description = "名称")
    private String name;

    @Schema(description = "值")
    private Object value;
}