package com.iusofts.basic.page;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * mysql 分页参数
 *
 * @author Ivan
 */
@Schema(description = "分页参数")
@Data
public class PageQuery {

    @Schema(description = "当前页", example = "1")
    private int currentPage = 1;

    @Schema(description = "每页记录数", example = "10")
    private int pageSize = 10;

    @Schema(description = "是否导出模板")
    private boolean template;

    @Schema(description = "总记录数", hidden = true)
    private int totalCount;

    @Schema(description = "总页数", hidden = true)
    private int totalPage;

    @Schema(hidden = true)
    private int start;

    public int getStart() {
        return (currentPage - 1) * pageSize;
    }

    @Override
    public String toString() {
        return "Pagination [totalCount=" + totalCount + ", totalPage=" + totalPage + ", currentPage=" + currentPage
                + ", pageSize=" + pageSize + "]";
    }
}
