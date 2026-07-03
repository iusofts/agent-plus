package com.iusofts.basic.page;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "分页返回结果")
@Data
public class PageResult<T> {

    @Schema(description = "总数")
    private long totalCount;

    @Schema(description = "列表数据")
    private List<T> dataList;

}

