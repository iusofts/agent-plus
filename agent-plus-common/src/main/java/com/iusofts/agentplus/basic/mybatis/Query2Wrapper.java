package com.iusofts.agentplus.basic.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

/**
 * @author Ivan
 */
public class Query2Wrapper<T> extends QueryWrapper<T> {

    @Override
    protected String columnToString(String column) {
        // 驼峰命名转换为下划线命名
        return StringUtils.camelToUnderline(column);
    }

}
