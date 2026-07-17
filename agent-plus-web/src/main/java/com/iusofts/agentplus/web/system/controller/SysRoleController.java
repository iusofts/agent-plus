package com.iusofts.agentplus.web.system.controller;

import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.basic.web.annotation.Permission;
import com.iusofts.agentplus.system.dto.SysDeptDto;
import com.iusofts.agentplus.system.dto.SysRoleDto;
import com.iusofts.agentplus.system.dto.SysUserDto;
import com.iusofts.agentplus.system.dto.SysUserRoleDto;
import com.iusofts.agentplus.system.interfaces.ISysDeptService;
import com.iusofts.agentplus.system.interfaces.ISysRoleService;
import com.iusofts.agentplus.system.interfaces.ISysUserService;
import com.iusofts.agentplus.system.service.SysPermissionService;
import com.iusofts.agentplus.web.common.controller.BApiController;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.iusofts.agentplus.web.common.util.SessionUtil.getUsername;

@RestController
@RequestMapping("/bapi/system/role")
public class SysRoleController extends BApiController {
    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysDeptService deptService;

    @Permission("system:role:query")
    @Operation(summary = "获取角色列表")
    @PostMapping("/list")
    public PageResult<SysRoleDto> list(@RequestBody SysRoleDto role) {
        return roleService.selectRoleList(role);
    }

    @Operation(summary = "根据角色编号获取详细信息")
    @GetMapping(value = "/{roleId}")
    public SysRoleDto getInfo(@PathVariable Long roleId) {
        roleService.checkRoleDataScope(roleId);
        return (roleService.selectRoleById(roleId));
    }

    @Permission("system:role:add")
    @Operation(summary = "新增角色")
    @PostMapping
    public int add(@Validated @RequestBody SysRoleDto role) {
        if (!roleService.checkRoleNameUnique(role)) {
            throw new SystemBusinessException("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        } else if (!roleService.checkRoleKeyUnique(role)) {
            throw new SystemBusinessException("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        role.setCreateBy(getUsername());
        return (roleService.insertRole(role));

    }

    @Permission("system:role:edit")
    @Operation(summary = "修改保存角色")
    @PutMapping
    public void edit(@Validated @RequestBody SysRoleDto role) {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        if (!roleService.checkRoleNameUnique(role)) {
            throw new SystemBusinessException("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
        } else if (!roleService.checkRoleKeyUnique(role)) {
            throw new SystemBusinessException("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        role.setUpdateBy(getUsername());

        if (roleService.updateRole(role) > 0) {
            return;
        }
        throw new SystemBusinessException("修改角色'" + role.getRoleName() + "'失败，请联系管理员");
    }

    @Operation(summary = "状态修改")
    @PutMapping("/changeStatus")
    public int changeStatus(@RequestBody SysRoleDto role) {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        role.setUpdateBy(getUsername());
        return (roleService.updateRoleStatus(role));
    }

    @Permission("system:role:remove")
    @Operation(summary = "删除角色")
    @DeleteMapping("/{roleIds}")
    public int remove(@PathVariable Long[] roleIds) {
        return (roleService.deleteRoleByIds(roleIds));
    }

    @Operation(summary = "获取角色选择框列表")
    @GetMapping("/optionselect")
    public List<SysRoleDto> optionselect() {
        return (roleService.selectRoleAll());
    }

    @Operation(summary = "查询已分配用户角色列表")
    @GetMapping("/authUser/allocatedList")
    public PageResult<SysUserDto> allocatedList(SysUserDto user) {
        return userService.selectAllocatedList(user);
    }

    @Operation(summary = "查询未分配用户角色列表")
    @GetMapping("/authUser/unallocatedList")
    public PageResult<SysUserDto> unallocatedList(SysUserDto user) {
        return userService.selectUnallocatedList(user);
    }

    @Operation(summary = "取消授权用户")
    @PutMapping("/authUser/cancel")
    public int cancelAuthUser(@RequestBody SysUserRoleDto userRole) {
        return (roleService.deleteAuthUser(userRole));
    }

    @Operation(summary = "批量取消授权用户")
    @PutMapping("/authUser/cancelAll")
    public int cancelAuthUserAll(Long roleId, Long[] userIds) {
        return (roleService.deleteAuthUsers(roleId, userIds));
    }

    @Operation(summary = "批量选择用户授权")
    @PutMapping("/authUser/selectAll")
    public int selectAuthUserAll(Long roleId, Long[] userIds) {
        roleService.checkRoleDataScope(roleId);
        return (roleService.insertAuthUsers(roleId, userIds));
    }

    @Operation(summary = "获取对应角色部门树列表")
    @GetMapping(value = "/deptTree/{roleId}")
    public Map<String,Object> deptTree(@PathVariable("roleId") Long roleId) {
        Map<String,Object> ajax = new HashMap<>();
        ajax.put("checkedKeys", deptService.selectDeptListByRoleId(roleId));
        ajax.put("depts", deptService.selectDeptTreeList(new SysDeptDto()));
        return ajax;
    }

    @Permission("system:role:edit")
    @Operation(summary = "数据权限")
    @PutMapping("/dataScope")
    public void dataScope(@RequestBody SysRoleDto role) {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        roleService.authDataScope(role);
    }
}
