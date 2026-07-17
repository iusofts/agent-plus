package com.iusofts.agentplus.system.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "后台用户修改头像参数")
public class UpdateAvatarReqVo {

    @Schema(description = "用户ID", hidden = true)
    private Long userId;

    @Schema(description = "头像地址")
    @NotBlank(message = "头像地址不能为空")
    private String avatar;

}
