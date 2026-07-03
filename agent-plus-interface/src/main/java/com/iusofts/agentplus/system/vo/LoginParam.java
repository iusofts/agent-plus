package com.iusofts.agentplus.system.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * @author Ivan Shen
 */
@Data
public class LoginParam {

    /**
     *用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     *密码
     */
    @NotBlank(message = "密码不能为空")
    @JSONField(serialize=false)
    private String password;

    private String imageId;

    @NotBlank(message = "验证码不能为空")
    private String imageCode;

    /** 最后登录IP */
    private String loginIp;

}
