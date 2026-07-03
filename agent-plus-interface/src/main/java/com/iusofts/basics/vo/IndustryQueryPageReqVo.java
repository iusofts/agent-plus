package com.iusofts.basics.vo;

import com.iusofts.basic.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class IndustryQueryPageReqVo extends PageQuery {
    @Schema(description = "行业名称")
    private String name;

    @Schema(description = "状态（1.启用 2.停用）")
    private Integer status;
}
