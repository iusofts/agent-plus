/*
 * Copyright (C) 2019 All rights reserved
 * Author: Ivan Shen
 * Date: 2025/5/14
 * Description:TokenVo.java
 */
package com.iusofts.system.vo;

import lombok.Data;

/**
 * @author Ivan Shen
 */
@Data
public class TokenVo {
    
    private String token;

    public TokenVo() {
    }

    public TokenVo(String token) {
        this.token = token;
    }
}
