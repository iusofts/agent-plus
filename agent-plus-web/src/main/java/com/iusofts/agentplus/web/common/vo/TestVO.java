package com.iusofts.agentplus.web.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalTime;

@Data
@Schema(description = "测试")
public class TestVO {
    
    @NotBlank(message = "名称不能为空")
    @Schema(description = "名称")
    private String name;

    @Schema(description = "时间")
    private LocalTime time;
    
}