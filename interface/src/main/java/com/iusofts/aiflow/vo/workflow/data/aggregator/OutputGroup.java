package com.iusofts.aiflow.vo.workflow.data.aggregator;

import com.iusofts.aiflow.vo.workflow.data.common.ParamMapKey;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 输出分组 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@Schema(description = "输出分组")
public class OutputGroup {

    @Schema(description = "分组名称")
    private String name;

    @Schema(description = "分组类型")
    private String type;

    @Schema(description = "聚合变量列表")
    private List<ParamMapKey> variables;

}
