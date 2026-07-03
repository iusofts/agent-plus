package com.iusofts.agentplus.web.system.controller;

import com.iusofts.agentplus.basic.annotation.OperationLogExclude;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.page.PageResult;
import com.iusofts.agentplus.basic.utils.StringUtils;
import com.iusofts.agentplus.basic.validation.ValidationUtils;
import com.iusofts.agentplus.basic.web.annotation.Permission;
import com.iusofts.agentplus.common.vo.ImportParam;
import com.iusofts.agentplus.system.dto.SysDeptDto;
import com.iusofts.agentplus.system.dto.SysRoleDto;
import com.iusofts.agentplus.system.dto.SysUserDto;
import com.iusofts.agentplus.system.dto.TreeSelectDto;
import com.iusofts.agentplus.system.interfaces.ISysDeptService;
import com.iusofts.agentplus.system.interfaces.ISysRoleService;
import com.iusofts.agentplus.system.interfaces.ISysUserExpandService;
import com.iusofts.agentplus.system.interfaces.ISysUserService;
import com.iusofts.agentplus.system.vo.BLoginUserVo;
import com.iusofts.agentplus.system.vo.EditPasswordReqVo;
import com.iusofts.agentplus.web.common.controller.BApiController;
import com.iusofts.agentplus.web.common.util.SessionUtil;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.iusofts.agentplus.basic.enums.OperationLogExcludeTypeEnums.RES;
import static com.iusofts.agentplus.web.common.util.SessionUtil.getUserId;
import static com.iusofts.agentplus.web.common.util.SessionUtil.getUsername;

@RestController
@RequestMapping("/bapi/system/user")
public class SysUserController extends BApiController {
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysDeptService deptService;
    @Resource
    private ISysUserExpandService userExpandService;

    @Permission("system:user:query")
    @OperationLogExclude(type = RES)
    @Operation(summary = "获取用户列表")
    @PostMapping("/list")
    public PageResult<SysUserDto> list(@RequestBody SysUserDto user) {
        return userService.selectUserList(user);
    }

    @GetMapping(value = {"/", "/{userId}"})
    @Operation(summary = "根据用户编号获取详细信息")
    public SysUserDto getInfo(@PathVariable(value = "userId", required = false) Long userId) {
        SysUserDto sysUser = new SysUserDto();
        if (StringUtils.isNotNull(userId)) {
            userService.checkUserDataScope(userId);
            sysUser = userService.selectUserById(userId);
            sysUser.setRoleIds(sysUser.getRoles().stream().map(SysRoleDto::getRoleId).collect(Collectors.toList()));
        }
        List<SysRoleDto> roles = roleService.selectRoleAll();
        sysUser.setRoles(SysUserDto.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        return sysUser;
    }

    @Permission("system:user:add")
    @Operation(summary = "新增用户")
    @PostMapping
    public int add(@Validated @RequestBody SysUserDto user) {
        if (!userService.checkUserNameUnique(user)) {
            throw new SystemBusinessException("新增用户'" + user.getUsername() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhone()) && !userService.checkPhoneUnique(user)) {
            throw new SystemBusinessException("新增用户'" + user.getUsername() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            throw new SystemBusinessException("新增用户'" + user.getUsername() + "'失败，邮箱账号已存在");
        }
        user.setCreateBy(getUsername());
        user.setPassword((user.getPassword()));
        return (userService.insertUser(user));
    }

    @Permission("system:user:edit")
    @Operation(summary = "修改用户")
    @PutMapping
    public int edit(@Validated @RequestBody SysUserDto user) {
        userService.checkUserAllowed(user);
        if (!userService.checkUserNameUnique(user)) {
            throw new SystemBusinessException("修改用户'" + user.getUsername() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhone()) && !userService.checkPhoneUnique(user)) {
            throw new SystemBusinessException("修改用户'" + user.getUsername() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            throw new SystemBusinessException("修改用户'" + user.getUsername() + "'失败，邮箱账号已存在");
        }
        user.setUpdateBy(getUsername());
        return (userService.updateUser(user));
    }

    @Permission("system:user:remove")
    @Operation(summary = "删除用户")
    @DeleteMapping("/{userIds}")
    public int remove(@PathVariable Long[] userIds) {
        if (ArrayUtils.contains(userIds, getUserId())) {
            throw new SystemBusinessException("当前用户不能删除");
        }
        return (userService.deleteUserByIds(userIds));
    }

    @Permission("system:user:resetPwd")
    @Operation(summary = "重置密码")
    @PutMapping("/resetPwd")
    public int resetPwd(@RequestBody SysUserDto user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setPassword((user.getPassword()));
        user.setUpdateBy(getUsername());
        return (userService.resetPwd(user));
    }

    @Operation(summary = "修改密码")
    @PostMapping("/editPassword")
    public void editPassword(@RequestBody EditPasswordReqVo reqVo) {
        reqVo.setOperatorId(getUserId());
        userService.editPassword(reqVo);
    }

    @Operation(summary = "状态修改")
    @PutMapping("/changeStatus")
    public int changeStatus(@RequestBody SysUserDto user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setUpdateBy(getUsername());
        return (userService.updateUserStatus(user));
    }

    @Operation(summary = "根据用户编号获取授权角色")
    @GetMapping("/authRole/{userId}")
    public Map<String,Object> authRole(@PathVariable("userId") Long userId) {
        Map<String,Object> ajax = new HashMap<>();
        SysUserDto user = userService.selectUserById(userId);
        List<SysRoleDto> roles = roleService.selectRolesByUserId(userId);
        ajax.put("user", user);
        ajax.put("roles", SysUserDto.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        return ajax;
    }

    @Operation(summary = "用户授权角色")
    @PutMapping("/authRole")
    public void insertAuthRole(Long userId, List<Long> roleIds) {
        userService.checkUserDataScope(userId);
        roleService.checkRoleDataScope(roleIds.toArray(new Long[0]));
        userService.insertUserAuth(userId, roleIds);
    }

    @Operation(summary = "获取部门树列表")
    @GetMapping("/deptTree")
    public List<TreeSelectDto> deptTree(SysDeptDto dept) {
        return (deptService.selectDeptTreeList(dept));
    }

    @Operation(summary = "批量新增门店员工")
    @PostMapping("/batchImportShopUser")
    public void batchImportShopUser(@RequestBody ImportParam param) {
        ValidationUtils.validate(param);
        BLoginUserVo sessionUser = SessionUtil.getBSessionUser();
        param.setCreaterId(sessionUser.getUser().getUserId());
        param.setCreaterName(sessionUser.getUser().getUsername());
        userExpandService.batchImportShopUser(param);
    }
}
