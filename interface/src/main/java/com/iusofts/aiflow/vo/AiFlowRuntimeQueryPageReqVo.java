package com.iusofts.aiflow.vo;

import com.iusofts.basic.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 流程运行实例 查询分页请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Data
public class AiFlowRuntimeQueryPageReqVo extends PageQuery {

    @Schema(description = "流程ID")
    private Long flowId;

    @Schema(description = "运行状态 0等待 1运行中 2成功 3失败 4终止")
    private Integer runStatus;

}
