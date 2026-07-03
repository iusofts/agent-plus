package com.iusofts.system.service;

import com.iusofts.basic.constants.UserConstants;
import com.iusofts.basic.utils.StringUtils;
import com.iusofts.system.dto.SysRoleDto;
import com.iusofts.system.dto.SysUserDto;
import com.iusofts.system.interfaces.ISysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户权限处理
 *
 * @author
 */
@Component
public class SysPermissionService {
    @Autowired
    private ISysRoleService roleService;

    /**
     * 获取角色数据权限
     *
     * @param user 用户信息
     * @return 角色权限信息
     */
    public Set<String> getRolePermission(SysUserDto user) {
        Set<String> roles = new HashSet<String>();
        // 管理员拥有所有权限
        if (user.isAdmin()) {
            roles.add("admin");
        } else {
            roles.addAll(roleService.selectRolePermissionByUserId(user.getUserId()));
        }
        return roles;
    }

    /**
     * 获取菜单数据权限
     *
     * @param user 用户信息
     * @return 菜单权限信息
     */
    public Set<String> getMenuPermission(SysUserDto user) {
        Set<String> perms = new HashSet<String>();
        // 管理员拥有所有权限
        if (user.isAdmin()) {
            perms.add("*:*:*");
        } else {
            List<SysRoleDto> roles = user.getRoles();
            if (!CollectionUtils.isEmpty(roles)) {
                // 直接读取角色 permissions 字段（菜单权限集合）
                for (SysRoleDto role : roles) {
                    if (StringUtils.equals(role.getStatus(), UserConstants.ROLE_NORMAL)) {
                        perms.addAll(roleService.getMenuPermsByRoleId(role.getRoleId()));
                    }
                }
            }
        }
        return perms;
    }
}
