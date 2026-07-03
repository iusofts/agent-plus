package com.iusofts.agentplus.ai.vo.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 汽车意向咨询消息VO
 *
 * @author AI
 */
@Data
@Schema(name = "AiConsultMessageVo", description = "意向咨询消息vo")
public class AiConsultMessageVo extends AiStructMessageVo {

    @Schema(description = "所在城市")
    private String city;

    @Schema(description = "意向型号")
    private String model;

    @Schema(description = "贷款意向")
    private String loans;

    @Schema(description = "是否留资 是：1 否：0")
    private Integer isLz;

    @Schema(description = "微信号")
    private String wx;

    @Schema(description = "手机号")
    private String mobile;
}