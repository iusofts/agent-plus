/*
 * Copyright (C) 2019 All rights reserved
 * Author: Ivan Shen
 * Date: 2025/2/7
 * Description:Update2Wrapper.java
 */
package com.iusofts.agentplus.basic.mybatis;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

/**
 * @author Ivan Shen
 */
public class Update2Wrapper<T> extends UpdateWrapper<T> {

    @Override
    public String columnToString(String column) {
        // 驼峰命名转换为下划线命名
        return StringUtils.camelToUnderline(column);
    }
    
}
