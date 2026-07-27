package com.iusofts.agentplus.library.vo.model;

import com.iusofts.agentplus.basic.web.vo.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * AI模型配置 查询分页请求对象
 * </p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiModelQueryPageReqVo extends PageQuery {

    @Schema(description = "模型类型 1:LLM 2:Embedding")
    private Integer modelType;

    @Schema(description = "模型提供商：qwen-阿里云(百炼平台)，doubao-字节跳动(火山引擎)，openai-OpenAI")
    private String provider;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "状态 0:禁用 1:启用")
    private Integer status;

    @Schema(description = "组织ID", hidden = true)
    private Integer orgId;

}
