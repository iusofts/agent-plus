/*
 * Copyright (C) 2019 All rights reserved
 * Author: Ivan Shen
 * Date: 2021/10/25
 * Description:PermissionUtil.java
 */
package com.iusofts.basic.utils;

import com.iusofts.basic.company.CompanyIdentityContextHolder;
import com.iusofts.basic.exception.InvalidPermissionException;

import java.util.Objects;

/**
 * 权限校验工具
 *
 * @author Ivan Shen
 */
public class PermissionUtil {

    /**
     * 检查数据权限
     * @param dataCompanyId
     */
    public static void checkDataPermission(Integer dataCompanyId) {
        Integer companyId = CompanyIdentityContextHolder.getCompanyId();
        if (Objects.nonNull(companyId) && companyId != 1) {
            if (!dataCompanyId.equals(companyId)) {
                throw new InvalidPermissionException("没有数据权限");
            }
        }
    }
    
}
