/*
 * Copyright (C) 2019 All rights reserved
 * Author: Ivan Shen
 * Date: 2025/5/14
 * Description:LoginServiceImpl.java
 */
package com.iusofts.agentplus.system.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.image.EasyCaptchaImgCodeUtil;
import com.iusofts.agentplus.basic.image.ImgIdentifyingCodeVO;
import com.iusofts.agentplus.basic.utils.JsonUtils;
import com.iusofts.agentplus.basic.security.MD5Util;
import com.iusofts.agentplus.system.dao.SysRoleMenuMapper;
import com.iusofts.agentplus.system.dto.SysUserDto;
import com.iusofts.agentplus.system.interfaces.ILoginService;
import com.iusofts.agentplus.system.interfaces.ISysRoleService;
import com.iusofts.agentplus.system.interfaces.ISysUserService;
import com.iusofts.agentplus.system.vo.BLoginUserVo;
import com.iusofts.agentplus.system.vo.ImageVerifyCodeVO;
import com.iusofts.agentplus.system.vo.LoginParam;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.iusofts.agentplus.basic.constants.CacheConstants.CAPTCHA_CODE_KEY;
import static com.iusofts.agentplus.basic.constants.CacheConstants.LOGIN_TOKEN_KEY;
import static com.iusofts.agentplus.basic.constants.UserConstants.USER_DISABLE;

/**
 * @author Ivan Shen
 */
@DS("sys")
@Service
public class LoginServiceImpl implements ILoginService {

    @Resource
    private ISysUserService sysUserService;

    @Resource
    private ISysRoleService sysRoleService;

    @Resource
    private SysPermissionService sysPermissionService;

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public String login(LoginParam param) {
        // 校验验证码
        boolean verifyImageCode = verifyImageCode(param.getImageId(), param.getImageCode());
        if (!verifyImageCode) {
            throw new SystemBusinessException("验证码输入错误");
        }
        SysUserDto sysUser = sysUserService.selectUserByUserName(param.getUsername());
        if (sysUser == null) {
            throw new SystemBusinessException("用户名或密码错误");
        }
        if (sysUser.getStatus().equals(USER_DISABLE)) {
            throw new SystemBusinessException("您的账号已被冻结，重新恢复请联系管理员。");
        }

        // 检查登录失败次数限制
        String loginFailKey = "b_login_fail_count:" + param.getUsername();
        RBucket<Integer> bucket = redissonClient.getBucket(loginFailKey);
        Integer failCount = bucket.get();

        // 如果失败次数超过限制（例如5次），则阻止登录一段时间（例如10分钟）
        if (failCount != null && failCount >= 5) {
            throw new SystemBusinessException("登录失败次数过多，请10分钟后再试");
        }

        if (!MD5Util.hex(param.getPassword()).equals(sysUser.getPassword())) {
            // 登录失败，增加失败计数
            if (failCount == null) {
                bucket.set(1, java.time.Duration.ofMinutes(10)); // 10分钟后过期
            } else {
                bucket.set(failCount + 1, java.time.Duration.ofMinutes(10));
            }
            throw new SystemBusinessException("用户名或密码错误");
        }

        // 登录成功，清除失败计数
        bucket.delete();

        recordLoginInfo(sysUser.getUserId(), param.getLoginIp());
        return login(sysUser);
    }

    @Override
    public ImageVerifyCodeVO getImageVerifyCode() {
        ImgIdentifyingCodeVO imageCode = EasyCaptchaImgCodeUtil.getImgEquationCode();
        String imageId = MD5Util.hex(UUID.randomUUID().toString() + System.currentTimeMillis());
        redissonClient.getBucket(CAPTCHA_CODE_KEY + imageId, StringCodec.INSTANCE)
                .set(imageCode.getImgCode(), Duration.ofSeconds(5 * 60));

        ImageVerifyCodeVO imageInfo = new ImageVerifyCodeVO();
        imageInfo.setImage(imageCode.getImg());
        imageInfo.setImageId(imageId);

        return imageInfo;
    }

    public boolean verifyImageCode(String imageId, String code) {
        if (StringUtils.isEmpty(imageId) || StringUtils.isEmpty(code)) {
            return false;
        }
        String cacheCode = (String) redissonClient.getBucket(CAPTCHA_CODE_KEY + imageId, StringCodec.INSTANCE).get();
        if (StringUtils.isEmpty(cacheCode)) {
            return false;
        }
        redissonClient.getKeys().delete(CAPTCHA_CODE_KEY + imageId);
        if (code.toLowerCase().equals(cacheCode.toLowerCase())) {
            return true;
        }

        return false;
    }

    @Override
    public List<Long> getMenuIds(Long userId) {
        Set<Long> menuIds = new HashSet<>();
        // 管理员拥有所有菜单权限，这里暂时返回空，后续根据需求调整
        if (SysUserDto.isAdmin(userId)) {
            return List.of();
        }
        // 获取用户的角色ID列表
        List<Long> roleIds = sysRoleService.selectRoleListByUserId(userId);
        // 根据角色ID获取菜单ID
        for (Long roleId : roleIds) {
            List<Long> roleMenuIds = sysRoleMenuMapper.selectMenuIdsByRoleId(roleId);
            menuIds.addAll(roleMenuIds);
        }
        return menuIds.stream().toList();
    }

    private String login(SysUserDto user) {
        // 生成token
        String token = UUID.randomUUID().toString();

        BLoginUserVo bLoginUserVo = new BLoginUserVo();
        user.setPassword(null);
        bLoginUserVo.setUser(user);
        bLoginUserVo.setRoles(sysPermissionService.getRolePermission(user));
        bLoginUserVo.setPermissions(sysPermissionService.getMenuPermission(user));
        // 保存到redis
        redissonClient.getBucket(LOGIN_TOKEN_KEY + token, StringCodec.INSTANCE)
                .set(JsonUtils.obj2json(bLoginUserVo), Duration.ofSeconds(60 * 60 * 24 * 3));
        return token;
    }

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId, String ip) {
        SysUserDto sysUser = new SysUserDto();
        sysUser.setUserId(userId);
        sysUser.setLoginIp(ip);
        sysUser.setLoginDate(LocalDateTime.now());
        sysUserService.updateUserProfile(sysUser);
    }

}
