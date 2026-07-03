package com.iusofts.basic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 解析excel入参
 *
 * @author mabiao
 */
@Data
@Schema(description = "解析excel入参")
public class AnalyExcelParam {

    @Schema(description = "路径")
    @NotBlank(message = "路径不能为空")
    private String filePath;
    
    @Schema(description = "解析列数")
    private List<Integer> cells;
    
    @Schema(description = "解析列参数")
    private List<String> cellNames;

    @Schema(description = "sheet位置")
    private Integer sheetAt;
}
