package com.iusofts.agentplus.system.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.basic.constants.UserConstants;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.page.PageResult;
import com.iusofts.agentplus.basic.utils.JsonUtils;
import com.iusofts.agentplus.basic.utils.ModelMapperUtil;
import com.iusofts.agentplus.basic.utils.StringUtils;
import com.iusofts.agentplus.system.dao.SysRoleDeptMapper;
import com.iusofts.agentplus.system.dao.SysRoleMapper;
import com.iusofts.agentplus.system.dao.SysRoleMenuMapper;
import com.iusofts.agentplus.system.dao.SysUserRoleMapper;
import com.iusofts.agentplus.system.dto.SysRoleDto;
import com.iusofts.agentplus.system.dto.SysRoleMenuDto;
import com.iusofts.agentplus.system.dto.SysUserDto;
import com.iusofts.agentplus.system.dto.SysUserRoleDto;
import com.iusofts.agentplus.system.entity.SysRole;
import com.iusofts.agentplus.system.entity.SysRoleDept;
import com.iusofts.agentplus.system.interfaces.ISysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 角色 业务层处理
 *
 * @author
 */
@DS("sys")
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {
    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleDeptMapper roleDeptMapper;

    /**
     * 根据条件分页查询角色数据
     *
     * @param role 角色信息
     * @return 角色数据集合信息
     */
    @Override
    public PageResult<SysRoleDto> selectRoleList(SysRoleDto role) {
        PageResult<SysRoleDto> pageResult = new PageResult<>();
        Page pageParam = new Page<>(role.getCurrentPage(), role.getPageSize());
        List<SysRole> sysRoles = roleMapper.selectRoleList(pageParam, role);
        pageResult.setDataList(ModelMapperUtil.strictMapList(sysRoles, SysRoleDto.class));
        pageResult.setTotalCount(pageParam.getTotal());
        return pageResult;
    }

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @Override
    public List<SysRoleDto> selectRolesByUserId(Long userId) {
        List<SysRoleDto> userRoles = ModelMapperUtil.strictMapList(roleMapper.selectRolePermissionByUserId(userId), SysRoleDto.class);
        List<SysRoleDto> roles = selectRoleAll();
        for (SysRoleDto role : roles) {
            for (SysRoleDto userRole : userRoles) {
                if (role.getRoleId().longValue() == userRole.getRoleId().longValue()) {
                    role.setFlag(true);
                    break;
                }
            }
        }
        return roles;
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectRolePermissionByUserId(Long userId) {
        List<SysRole> perms = roleMapper.selectRolePermissionByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (SysRole perm : perms) {
            if (StringUtils.isNotNull(perm)) {
                permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    @Override
    public List<SysRoleDto> selectRoleAll() {
        SysRoleDto sysRole = new SysRoleDto();
        sysRole.setPageSize(999);
        PageResult<SysRoleDto> pageResult = selectRoleList(sysRole);
        return pageResult.getDataList();
    }

    /**
     * 根据用户ID获取角色选择框列表
     *
     * @param userId 用户ID
     * @return 选中角色ID列表
     */
    @Override
    public List<Long> selectRoleListByUserId(Long userId) {
        return roleMapper.selectRoleListByUserId(userId);
    }

    /**
     * 通过角色ID查询角色
     *
     * @param roleId 角色ID
     * @return 角色对象信息
     */
    @Override
    public SysRoleDto selectRoleById(Long roleId) {
        SysRole role = roleMapper.selectRoleById(roleId);
        if (role == null) {
            return null;
        }
        SysRoleDto dto = ModelMapperUtil.strictMap(role, SysRoleDto.class);
        List<Long> menuIds = roleMenuMapper.selectMenuIdsByRoleId(roleId);
        dto.setMenuIds(menuIds.toArray(new Long[0]));
        return dto;
    }

    /**
     * 校验角色名称是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public boolean checkRoleNameUnique(SysRoleDto role) {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole info = roleMapper.checkRoleNameUnique(role.getRoleName());
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验角色权限是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public boolean checkRoleKeyUnique(SysRoleDto role) {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole info = roleMapper.checkRoleKeyUnique(role.getRoleKey());
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验角色是否允许操作
     *
     * @param role 角色信息
     */
    @Override
    public void checkRoleAllowed(SysRoleDto role) {
        if (StringUtils.isNotNull(role.getRoleId()) && role.isAdmin()) {
            throw new SystemBusinessException("不允许操作超级管理员角色");
        }
    }

    /**
     * 校验角色是否有数据权限
     *
     * @param roleIds 角色id
     */
    @Override
    public void checkRoleDataScope(Long... roleIds) {
        //if (!SysUser.isAdmin(SecurityUtils.getUserId())) {
        if (!SysUserDto.isAdmin(1L)) {
            for (Long roleId : roleIds) {
                SysRoleDto role = new SysRoleDto();
                role.setRoleId(roleId);
                List<SysRole> roles = roleMapper.selectRoleList(null, role);
                if (StringUtils.isEmpty(roles)) {
                    throw new SystemBusinessException("没有权限访问角色数据！");
                }
            }
        }
    }

    /**
     * 通过角色ID查询角色使用数量
     *
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    public int countUserRoleByRoleId(Long roleId) {
        return userRoleMapper.countUserRoleByRoleId(roleId);
    }

    /**
     * 新增保存角色信息
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertRole(SysRoleDto role) {
        // 新增角色信息（含菜单权限集合，转 JSON 串一并写入）
        SysRole entity = toEntity(role);
        roleMapper.insertRole(entity);
        // 回写自增主键
        role.setRoleId(entity.getRoleId());
        return insertRoleMenu(role);
    }

    /**
     * 修改保存角色信息
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateRole(SysRoleDto role) {
        // 修改角色信息（含菜单权限集合，转 JSON 串一并更新）
        roleMapper.updateRole(toEntity(role));
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenuByRoleId(role.getRoleId());
        return insertRoleMenu(role);
    }

    /**
     * 将角色 DTO 转换为实体，并把菜单权限集合（Set）转为 JSON 数组字符串写入 permissions
     *
     * @param role 角色信息
     * @return 角色实体
     */
    private SysRole toEntity(SysRoleDto role) {
        SysRole entity = ModelMapperUtil.strictMap(role, SysRole.class);
        if (role.getPermissions() != null) {
            entity.setPermissions(JsonUtils.obj2json(role.getPermissions()));
        }
        return entity;
    }

    /**
     * 读取 sys_role.permissions（JSON 数组字符串）并转换为菜单权限集合
     *
     * @param roleId 角色ID
     * @return 菜单权限集合
     */
    @Override
    public Set<String> getMenuPermsByRoleId(Long roleId) {
        SysRole role = getById(roleId);
        if (role == null || StringUtils.isEmpty(role.getPermissions())) {
            return Collections.emptySet();
        }
        return new HashSet<>(JsonUtils.json2list(role.getPermissions(), String.class));
    }

    @Override
    @Transactional
    public int authDataScope(SysRoleDto role) {
        // 修改角色信息
        roleMapper.updateRole(ModelMapperUtil.strictMap(role, SysRole.class));
        // 删除角色与部门关联
        roleDeptMapper.deleteRoleDeptByRoleId(role.getRoleId());
        // 新增角色和部门信息（数据权限）
        return insertRoleDept(role);
    }

    /**
     * 修改角色状态
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public int updateRoleStatus(SysRoleDto role) {
        return roleMapper.updateRole(toEntity(role));
    }

    /**
     * 新增角色菜单信息
     *
     * @param role 角色对象
     */
    public int insertRoleMenu(SysRoleDto role) {
        int rows = 1;
        // 新增用户与角色管理
        List<SysRoleMenuDto> list = new ArrayList<SysRoleMenuDto>();
        for (Long menuId : role.getMenuIds()) {
            SysRoleMenuDto rm = new SysRoleMenuDto();
            rm.setRoleId(role.getRoleId());
            rm.setMenuId(menuId);
            list.add(rm);
        }
        if (list.size() > 0) {
            rows = roleMenuMapper.batchRoleMenu(list);
        }
        return rows;
    }


    /**
     * 通过角色ID删除角色
     *
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteRoleById(Long roleId) {
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenuByRoleId(roleId);
        return roleMapper.deleteRoleById(roleId);
    }

    /**
     * 批量删除角色信息
     *
     * @param roleIds 需要删除的角色ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteRoleByIds(Long[] roleIds) {
        for (Long roleId : roleIds) {
            checkRoleAllowed(new SysRoleDto(roleId));
            checkRoleDataScope(roleId);
            SysRoleDto role = selectRoleById(roleId);
            if (countUserRoleByRoleId(roleId) > 0) {
                throw new SystemBusinessException(String.format("%1$s已分配,不能删除", role.getRoleName()));
            }
        }
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenu(roleIds);
        return roleMapper.deleteRoleByIds(roleIds);
    }

    /**
     * 取消授权用户角色
     *
     * @param userRole 用户和角色关联信息
     * @return 结果
     */
    @Override
    public int deleteAuthUser(SysUserRoleDto userRole) {
        return userRoleMapper.deleteUserRoleInfo(userRole);
    }

    /**
     * 批量取消授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要取消授权的用户数据ID
     * @return 结果
     */
    @Override
    public int deleteAuthUsers(Long roleId, Long[] userIds) {
        return userRoleMapper.deleteUserRoleInfos(roleId, userIds);
    }

    /**
     * 批量选择授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要授权的用户数据ID
     * @return 结果
     */
    @Override
    public int insertAuthUsers(Long roleId, Long[] userIds) {
        // 新增用户与角色管理
        List<SysUserRoleDto> list = new ArrayList<SysUserRoleDto>();
        for (Long userId : userIds) {
            SysUserRoleDto ur = new SysUserRoleDto();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            list.add(ur);
        }
        return userRoleMapper.batchUserRole(list);
    }

    @Override
    public List<SysRoleDto> getRolesByNames(List<String> roleNames) {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysRole::getRoleName, roleNames);
        return ModelMapperUtil.strictMapList(super.list(queryWrapper), SysRoleDto.class);
    }

    /**
     * 新增角色部门信息(数据权限)
     *
     * @param role 角色对象
     */
    public int insertRoleDept(SysRoleDto role) {
        int rows = 1;
        // 新增角色与部门（数据权限）管理
        List<SysRoleDept> list = new ArrayList<SysRoleDept>();
        for (Long deptId : role.getDeptIds()) {
            SysRoleDept rd = new SysRoleDept();
            rd.setRoleId(role.getRoleId());
            rd.setDeptId(deptId);
            list.add(rd);
        }
        if (list.size() > 0) {
            rows = roleDeptMapper.batchRoleDept(list);
        }
        return rows;
    }

}
