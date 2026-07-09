package com.iusofts.agentplus.ailog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * LLM 调用日志分页结果。
 *
 * @author Ivan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "LLM 调用日志分页结果")
public class LlmCallLogPageResult {

    @Schema(description = "总条数")
    private Long total;

    @Schema(description = "当前页数据")
    private List<LlmCallLogItem> items;
}
