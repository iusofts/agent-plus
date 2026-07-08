package com.iusofts.agentplus.basic.web.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 手机号查询
 *
 * @author 
 */
@Data
@Schema(description = "手机号查询")
public class PhoneParam {

    @Schema(description = "手机号")
    private String phone;

}
