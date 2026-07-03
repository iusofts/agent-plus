package com.iusofts.aiflow.vo.workflow.data.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 输出参数 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@Schema(description = "输出参数")
public class OutputParam {

    @Schema(description = "参数名称")
    private String name;

    @Schema(description = "参数类型")
    private String type;

    @Schema(description = "参数描述")
    private String description;

    @Schema(description = "参数映射键")
    private ParamMapKey paramMapKey;

}
