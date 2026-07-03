package com.iusofts.agentplus.system.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * 系统角色枚举
 * Java8 规范定义，包含角色编码、中文名称，提供通用匹配/获取方法
 * @author xxx
 * @date 2026-01-29
 */
public enum SysRoleEnum {
    // 枚举常量：角色编码(优化后英文)，角色中文名称
    SUPER_ADMIN("超级管理员"),
    NORMAL_USER("普通角色"),
    STORE_STAFF("门店员工"),
    HQ_STAFF("总部员工"),
    SALES_CONSULT("销售顾问"),
    INFORMATION_CLERK("信息员"),
    TEST("测试角色"),
    ;

    // 角色中文名称
    private final String roleName;

    /**
     * 枚举构造方法（默认private，无需显式声明）
     * @param roleName 角色中文名称
     */
    SysRoleEnum(String roleName) {
        this.roleName = roleName;
    }

    // ========== 基础getter方法 ==========
    /**
     * 获取角色编码（即枚举常量名，如SUPER_ADMIN）
     */
    public String getRoleCode() {
        return this.name();
    }

    /**
     * 获取角色中文名称
     */
    public String getRoleName() {
        return roleName;
    }

    // ========== 通用工具方法（开发常用） ==========
    /**
     * 根据角色编码匹配枚举（避免空指针/非法参数异常）
     * Java8+ 推荐使用Optional封装返回值，优雅处理空值
     * @param roleCode 角色编码（如SUPER_ADMIN）
     * @return 匹配的枚举Optional，无匹配则返回Optional.empty()
     */
    public static Optional<SysRoleEnum> getByCode(String roleCode) {
        // 先判空，避免name()方法空指针
        if (roleCode == null || StringUtils.isBlank(roleCode)) {
            return Optional.empty();
        }
        // 遍历枚举匹配编码，避免使用valueOf()的非法参数异常
        for (SysRoleEnum role : SysRoleEnum.values()) {
            if (role.getRoleCode().equals(roleCode)) {
                return Optional.of(role);
            }
        }
        return Optional.empty();
    }

    /**
     * 简化版：根据编码获取枚举，无匹配则返回null（适合简单场景）
     * @param roleCode 角色编码
     * @return 匹配的枚举/ null
     */
    public static SysRoleEnum getByCodeSimple(String roleCode) {
        return getByCode(roleCode).orElse(null);
    }

    /**
     * 判断指定编码是否为有效角色
     * @param roleCode 角色编码
     * @return true=有效，false=无效/空
     */
    public static boolean isValidRole(String roleCode) {
        return getByCode(roleCode).isPresent();
    }
}