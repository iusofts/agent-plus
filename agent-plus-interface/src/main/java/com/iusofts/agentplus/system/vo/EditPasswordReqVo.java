package com.iusofts.agentplus.system.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description =  "后台用户修改密码参数")
public class EditPasswordReqVo {

    @Schema(description = "原密码")
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @Schema(description = "新密码")
    @NotBlank(message = "新密码不能为空")
    private String password;

    @Schema(description = "操作人ID", hidden = true)
    private Long operatorId;

}
