package com.iusofts.system.vo;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * @author Ivan Shen
 */
@Data
public class LoginCodeParam {

    @NotBlank(message = "登录码不能为空")
    private String code;

}
