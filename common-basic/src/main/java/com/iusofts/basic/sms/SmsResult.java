package com.iusofts.basic.sms;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 短信发送结果回执
 *
 * @author 
 * @date 2021/1/18
 */
@Data
@Schema(description = "短信发送结果回执")
public class SmsResult {

    @Schema(description = "请求ID")
    private String RequestId;

    @Schema(description = "状态码的描述。")
    private String Message;

    @Schema(description = "状态码。返回OK代表请求成功")
    private String Code;

}
