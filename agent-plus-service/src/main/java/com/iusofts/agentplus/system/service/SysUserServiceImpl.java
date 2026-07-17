package com.iusofts.agentplus.system.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.basic.constants.UserConstants;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.basic.security.MD5Util;
import com.iusofts.agentplus.basic.utils.ModelMapperUtil;
import com.iusofts.agentplus.basic.utils.StringUtils;
import com.iusofts.agentplus.system.dao.SysRoleMapper;
import com.iusofts.agentplus.system.dao.SysUserMapper;
import com.iusofts.agentplus.system.dao.SysUserRoleMapper;
import com.iusofts.agentplus.system.dto.SysRoleDto;
import com.iusofts.agentplus.system.dto.SysUserDto;
import com.iusofts.agentplus.system.dto.SysUserRoleDto;
import com.iusofts.agentplus.system.entity.SysRole;
import com.iusofts.agentplus.system.entity.SysUser;
import com.iusofts.agentplus.system.interfaces.ISysUserService;
import com.iusofts.agentplus.system.vo.EditPasswordReqVo;
import com.iusofts.agentplus.system.vo.UpdateAvatarReqVo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户 业务层处理
 *
 * @author
 */
@DS("sys")
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    /**
     * 根据条件分页查询用户列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    public PageResult<SysUserDto> selectUserList(SysUserDto user) {
        PageResult<SysUserDto> pageResult = new PageResult<>();
        Page pageParam = new Page<>(user.getCurrentPage(), user.getPageSize());
        List<SysUserDto> sysUserDtos = userMapper.selectUserList(pageParam, user);
        fillUserRoles(sysUserDtos);
        pageResult.setDataList(sysUserDtos);
        pageResult.setTotalCount(pageParam.getTotal());
        return pageResult;
    }

    /**
     * 批量查询并回填用户拥有的角色
     *
     * @param users 用户列表
     */
    private void fillUserRoles(List<SysUserDto> users) {
        if (CollectionUtils.isEmpty(users)) {
            return;
        }
        List<Long> userIds = users.stream().map(SysUserDto::getUserId).collect(Collectors.toList());
        List<SysUserDto> userRoles = userMapper.selectRolesByUserIds(userIds);
        Map<Long, List<SysRoleDto>> roleMap = userRoles.stream()
                .collect(Collectors.toMap(SysUserDto::getUserId, SysUserDto::getRoles));
        users.forEach(u -> u.setRoles(roleMap.getOrDefault(u.getUserId(), new ArrayList<>())));
    }

    /**
     * 根据条件分页查询已分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    public PageResult<SysUserDto> selectAllocatedList(SysUserDto user) {
        PageResult<SysUserDto> pageResult = new PageResult<>();
        Page pageParam = new Page<>(user.getCurrentPage(), user.getPageSize());
        pageResult.setDataList(userMapper.selectAllocatedList(pageParam, user));
        pageResult.setTotalCount(pageParam.getTotal());
        return pageResult;
    }

    /**
     * 根据条件分页查询未分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    public PageResult<SysUserDto> selectUnallocatedList(SysUserDto user) {
        PageResult<SysUserDto> pageResult = new PageResult<>();
        Page pageParam = new Page<>(user.getCurrentPage(), user.getPageSize());
        pageResult.setDataList(userMapper.selectUnallocatedList(pageParam, user));
        pageResult.setTotalCount(pageParam.getTotal());
        return pageResult;
    }

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    @Override
    public SysUserDto selectUserByUserName(String userName) {
        return userMapper.selectUserByUserName(userName);
    }

    @Override
    public List<SysUserDto> selectUserByNames(List<String> names, Integer shopId) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysUser::getName, names);
        return ModelMapperUtil.strictMapList(super.list(queryWrapper), SysUserDto.class);
    }

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    @Override
    public SysUserDto selectUserById(Long userId) {
        return userMapper.selectUserById(userId);
    }

    /**
     * 查询用户所属角色组
     *
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String selectUserRoleGroup(String userName) {
        List<SysRole> list = roleMapper.selectRolesByUserName(userName);
        if (CollectionUtils.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysRole::getRoleName).collect(Collectors.joining(","));
    }


    /**
     * 校验用户名称是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean checkUserNameUnique(SysUserDto user) {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUserDto info = userMapper.checkUserNameUnique(user.getUsername());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验用户名称是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public boolean checkPhoneUnique(SysUserDto user) {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUserDto info = userMapper.checkPhoneUnique(user.getPhone());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public boolean checkEmailUnique(SysUserDto user) {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUserDto info = userMapper.checkEmailUnique(user.getEmail());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验用户是否允许操作
     *
     * @param user 用户信息
     */
    @Override
    public void checkUserAllowed(SysUserDto user) {
        if (StringUtils.isNotNull(user.getUserId()) && user.isAdmin()) {
            throw new SystemBusinessException("不允许操作超级管理员用户");
        }
    }

    /**
     * 校验用户是否有数据权限
     *
     * @param userId 用户id
     */
    @Override
    public void checkUserDataScope(Long userId) {
        //if (!SysUser.isAdmin(SecurityUtils.getUserId()))
        if (!SysUserDto.isAdmin(1L)) {
            SysUserDto user = new SysUserDto();
            user.setUserId(userId);
            List<SysUserDto> users = userMapper.selectUserList(null, user);
            if (StringUtils.isEmpty(users)) {
                throw new SystemBusinessException("没有权限访问用户数据！");
            }
        }
    }

    /**
     * 新增保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertUser(SysUserDto user) {
        // 新增用户信息
        user.setPassword(MD5Util.hex(user.getPassword()));
        int rows = userMapper.insertUser(user);
        // 新增用户与角色管理
        insertUserRole(user);
        return rows;
    }

    /**
     * 注册用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional
    public boolean registerUser(SysUserDto user) {
        boolean success = userMapper.insertUser(user) > 0;
        return success;
    }

    /**
     * 修改保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateUser(SysUserDto user) {
        Long userId = user.getUserId();
        // 删除用户与角色关联
        userRoleMapper.deleteUserRoleByUserId(userId);
        // 新增用户与角色管理
        insertUserRole(user);
        return userMapper.updateUser(user);
    }

    /**
     * 用户授权角色
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     */
    @Override
    @Transactional
    public void insertUserAuth(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteUserRoleByUserId(userId);
        insertUserRole(userId, roleIds);
    }

    /**
     * 修改用户状态
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserStatus(SysUserDto user) {
        return userMapper.updateUser(user);
    }

    /**
     * 修改用户基本信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserProfile(SysUserDto user) {
        return userMapper.updateUser(user);
    }

    /**
     * 修改用户头像
     *
     * @param userName 用户名
     * @param avatar   头像地址
     * @return 结果
     */
    @Override
    public boolean updateUserAvatar(String userName, String avatar) {
        return userMapper.updateUserAvatar(userName, avatar) > 0;
    }

    /**
     * 根据用户ID修改用户头像
     *
     * @param reqVo 修改头像参数
     */
    @Override
    public void updateUserAvatar(UpdateAvatarReqVo reqVo) {
        SysUserDto user = new SysUserDto();
        user.setUserId(reqVo.getUserId());
        user.setAvatar(reqVo.getAvatar());
        userMapper.updateUser(user);
    }

    /**
     * 重置用户密码
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int resetPwd(SysUserDto user) {
        user.setPassword(MD5Util.hex(user.getPassword()));
        return userMapper.updateUser(user);
    }

    @Override
    public void editPassword(EditPasswordReqVo reqVo) {
        SysUser sysUser = super.getById(reqVo.getOperatorId());
        if (!MD5Util.hex(reqVo.getOldPassword()).equals(sysUser.getPassword())) {
            throw new SystemBusinessException("原密码错误");
        }
        sysUser.setPassword(MD5Util.hex(reqVo.getPassword()));
        sysUser.setUpdateTime(LocalDateTime.now());
        super.updateById(sysUser);
    }

    /**
     * 新增用户角色信息
     *
     * @param user 用户对象
     */
    public void insertUserRole(SysUserDto user) {
        this.insertUserRole(user.getUserId(), user.getRoleIds());
    }

    /**
     * 新增用户角色信息
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     */
    public void insertUserRole(Long userId, List<Long> roleIds) {
        if (StringUtils.isNotEmpty(roleIds)) {
            // 新增用户与角色管理
            List<SysUserRoleDto> list = new ArrayList<>();
            for (Long roleId : roleIds) {
                SysUserRoleDto ur = new SysUserRoleDto();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                list.add(ur);
            }
            userRoleMapper.batchUserRole(list);
        }
    }

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteUserById(Long userId) {
        // 删除用户与角色关联
        userRoleMapper.deleteUserRoleByUserId(userId);
        return userMapper.deleteUserById(userId);
    }

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteUserByIds(Long[] userIds) {
        for (Long userId : userIds) {
            checkUserAllowed(new SysUserDto(userId));
            checkUserDataScope(userId);
        }
        // 删除用户与角色关联
        userRoleMapper.deleteUserRole(userIds);
        return userMapper.deleteUserByIds(userIds);
    }

}
