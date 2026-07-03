package com.iusofts.basic.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 图片标签数据
 *
 * @author Ivan Shen
 */
@Data
public class ImageLabel {

    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "顶坐标百分比")
    private String top;

    @Schema(description = "左坐标百分比")
    private String left;

    @Schema(description = "是否向左 1：左 2：右")
    private Integer isLeftDot;

    @Schema(description = "类型 1：普通 2：商品")
    private Integer type;

    @Schema(description = "商品ID")
    private Integer skuId;

}
