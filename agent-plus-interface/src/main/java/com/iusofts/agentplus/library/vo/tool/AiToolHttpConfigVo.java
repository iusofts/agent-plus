package com.iusofts.agentplus.library.vo.tool;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 工具HTTP 配置.
 *
 * @author Ivan
 */
@Data
@Schema(description = "工具HTTP配置vo")
public class AiToolHttpConfigVo {

    @NotBlank(message = "接口路径不能为空")
    @Schema(description = "接口路径")
    private String uri;

    @Schema(description = "请求方法")
    private String method;

}

