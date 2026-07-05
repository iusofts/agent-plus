package com.iusofts.agentplus.aiflow.vo.workflow.data;

import com.iusofts.agentplus.aiflow.vo.workflow.data.common.Param;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * <p>
 * 开始节点数据 数据传输对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "开始节点数据")
public class StartNodeData extends NodeData {

    @Schema(description = "输入参数列表")
    private List<Param> inputParams;

}
