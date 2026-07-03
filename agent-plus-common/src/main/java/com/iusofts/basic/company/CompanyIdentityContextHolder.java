package com.iusofts.basic.company;

import lombok.extern.slf4j.Slf4j;

/**
 * 公司身份id上下文线程变量
 *
 * @author 
 * @date 2020/11/30
 */
@Slf4j
public class CompanyIdentityContextHolder {

    private static final ThreadLocal<Integer> CONTEXT_HOLDER = new ThreadLocal<>();

    public static void setCompanyId(Integer companyId) {
        CONTEXT_HOLDER.set(companyId);
    }

    public static Integer getCompanyId() {
        return CONTEXT_HOLDER.get();
    }

    public static void clearCompanyId() {
        CONTEXT_HOLDER.remove();
    }

}
