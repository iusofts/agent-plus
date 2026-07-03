package com.iusofts.basic.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.iusofts.basic.company.CompanyIdentityContextHolder;

import java.util.Objects;

/**
 * @author Ivan
 */
public class Query2Wrapper<T> extends QueryWrapper<T> {

    public Query2Wrapper(boolean flag) {
        if (flag) {
            Integer companyId = CompanyIdentityContextHolder.getCompanyId();
            if (Objects.nonNull(companyId) && companyId != 1) {
                eq("companyId", companyId);
            }
        }
    }

    public Query2Wrapper() {
        this(true);
    }

    @Override
    protected String columnToString(String column) {
        // 驼峰命名转换为下划线命名
        return StringUtils.camelToUnderline(column);
    }

}
