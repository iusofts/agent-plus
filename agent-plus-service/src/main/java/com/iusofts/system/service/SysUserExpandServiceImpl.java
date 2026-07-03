/*
 * Copyright (C) 2019 All rights reserved
 * Author: Ivan Shen
 * Date: 2026/1/29
 * Description:SysUserQueryServiceImpl.java
 */
package com.iusofts.system.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.basic.exception.SystemBusinessException;
import com.iusofts.basic.utils.ExcelUtil;
import com.iusofts.basic.utils.PinyinTool;
import com.iusofts.basic.utils.PinyinTool.Type;
import com.iusofts.common.vo.ImportParam;
import com.iusofts.system.dao.SysUserMapper;
import com.iusofts.system.dto.SysRoleDto;
import com.iusofts.system.dto.SysUserDto;
import com.iusofts.system.entity.SysRole;
import com.iusofts.system.entity.SysUser;
import com.iusofts.system.interfaces.ISysUserExpandService;
import com.iusofts.system.interfaces.ISysUserService;
import com.iusofts.system.vo.ShopUserExcelImportVo;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ivan Shen
 */
@DS("sys")
@Service
public class SysUserExpandServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserExpandService {

    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysRoleServiceImpl roleService;
    @Resource
    private ISysUserService sysUserService;

    @Override
    public List<SysUserDto> getSysUserList(Integer shopId, String roleCode) {
        SysUserDto sysUserParm = new SysUserDto();
        sysUserParm.setStatus("0");
        if (StringUtils.isNotBlank(roleCode)) {
            LambdaQueryWrapper<SysRole> roleQueryWrapper = new LambdaQueryWrapper<>();
            roleQueryWrapper.eq(SysRole::getRoleKey, roleCode);
            SysRole sysRole = roleService.getOne(roleQueryWrapper, false);
            if (sysRole != null) {
                sysUserParm.setRoleId(sysRole.getRoleId());
            }
        }
        List<SysUserDto> sysUserDtos = userMapper.selectUserList(null, sysUserParm);
        return sysUserDtos;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchImportShopUser(ImportParam param) {
        // 解析excel
        try {
            List<ShopUserExcelImportVo> importVos = ExcelUtil.importUrlExcel(param.getFileUrl(), ShopUserExcelImportVo.class);
            importVos = importVos.stream().filter(item -> StringUtils.isNotBlank(item.getName())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(importVos)) {
                Set<String> roleNames = importVos.stream()
                        .map(ShopUserExcelImportVo::getRoleName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                Collection<SysRoleDto> roles = roleService.getRolesByNames(new ArrayList<>(roleNames));
                Map<String, Long> roleIdMap = new HashMap<>();
                if (CollectionUtils.isNotEmpty(roles)) {
                    roles.forEach(item -> roleIdMap.put(item.getRoleName(), item.getRoleId()));
                }
                importVos.forEach(item -> {
                    SysUserDto sysUserDto = new SysUserDto();
                    sysUserDto.setName(item.getName());
                    if (StringUtils.isNotBlank(item.getUsername())) {
                        sysUserDto.setUsername(item.getUsername());
                    } else {
                        sysUserDto.setUsername(PinyinTool.toPinYin(item.getName(), "", Type.LOWERCASE));
                    }
                    sysUserDto.setPassword("123456");
                    sysUserDto.setPhone(item.getPhone());
                    Long roleId = roleIdMap.get(item.getRoleName());
                    if (roleId != null) {
                        sysUserDto.setRoleIds(Arrays.asList(roleId));
                    }
                    sysUserDto.setCreateBy(param.getCreaterName());
                    add(sysUserDto);
                });
            }
        } catch (SystemBusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析excel异常", e);
            throw new SystemBusinessException("解析excel异常！");
        }
    }


    public int add(SysUserDto user) {
        if (!sysUserService.checkUserNameUnique(user)) {
            throw new SystemBusinessException("新增用户'" + user.getUsername() + "'失败，登录账号已存在");
        } else if (com.iusofts.basic.utils.StringUtils.isNotEmpty(user.getPhone()) && !sysUserService.checkPhoneUnique(user)) {
            throw new SystemBusinessException("新增用户'" + user.getUsername() + "'失败，手机号码已存在");
        } else if (com.iusofts.basic.utils.StringUtils.isNotEmpty(user.getEmail()) && !sysUserService.checkEmailUnique(user)) {
            throw new SystemBusinessException("新增用户'" + user.getUsername() + "'失败，邮箱账号已存在");
        }
        return (sysUserService.insertUser(user));
    }

}
