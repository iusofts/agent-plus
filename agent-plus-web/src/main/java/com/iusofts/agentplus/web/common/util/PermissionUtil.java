package com.iusofts.agentplus.web.common.util;

import com.iusofts.agentplus.basic.web.annotation.Permission;
import com.iusofts.agentplus.system.vo.BLoginUserVo;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 权限工具
 *
 * @author Ivan
 */
public class PermissionUtil {

    /**
     * 校验权限
     *
     * @param permission 权限注解
     * @return 是否有权限
     */
    public static boolean checkPermission(Permission permission) {
        if (permission == null) {
            return true;
        }

        BLoginUserVo loginUser = SessionUtil.getBSessionUser();
        if (loginUser == null) {
            return false;
        }

        // 管理员拥有所有权限
        if (loginUser.getUser() != null && loginUser.getUser().isAdmin()) {
            return true;
        }

        // 校验角色权限
        if (StringUtils.isNotBlank(permission.role())) {
            if (!hasRole(loginUser, permission.role())) {
                return false;
            }
        }
        if (ArrayUtils.isNotEmpty(permission.roles())) {
            if (!hasAnyRole(loginUser, permission.roles())) {
                return false;
            }
        }

        // 校验菜单权限
        if (StringUtils.isNotBlank(permission.value())) {
            if (!hasPermission(loginUser, permission.value())) {
                return false;
            }
        }
        if (ArrayUtils.isNotEmpty(permission.values())) {
            if (!hasAnyPermission(loginUser, permission.values())) {
                return false;
            }
        }

        return true;
    }

    /**
     * 判断是否包含指定角色
     */
    private static boolean hasRole(BLoginUserVo loginUser, String role) {
        if (loginUser.getRoles() == null || loginUser.getRoles().isEmpty()) {
            return false;
        }
        return loginUser.getRoles().contains(role);
    }

    /**
     * 判断是否包含任意一个指定角色
     */
    private static boolean hasAnyRole(BLoginUserVo loginUser, String[] roles) {
        if (loginUser.getRoles() == null || loginUser.getRoles().isEmpty()) {
            return false;
        }
        return Arrays.stream(roles).anyMatch(loginUser.getRoles()::contains);
    }

    /**
     * 判断是否包含指定权限
     */
    private static boolean hasPermission(BLoginUserVo loginUser, String permission) {
        if (loginUser.getPermissions() == null || loginUser.getPermissions().isEmpty()) {
            return false;
        }
        // 检查是否有 "*:*:*" 全部权限
        if (loginUser.getPermissions().contains("*:*:*")) {
            return true;
        }
        return loginUser.getPermissions().contains(permission);
    }

    /**
     * 判断是否包含任意一个指定权限
     */
    private static boolean hasAnyPermission(BLoginUserVo loginUser, String[] permissions) {
        if (loginUser.getPermissions() == null || loginUser.getPermissions().isEmpty()) {
            return false;
        }
        // 检查是否有 "*:*:*" 全部权限
        if (loginUser.getPermissions().contains("*:*:*")) {
            return true;
        }
        return Arrays.stream(permissions).anyMatch(loginUser.getPermissions()::contains);
    }

}