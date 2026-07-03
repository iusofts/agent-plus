package com.iusofts.system.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iusofts.system.dto.SysRoleMenuDto;
import com.iusofts.system.entity.SysRoleMenu;

import java.util.List;

/**
 * 角色与菜单关联表 数据层
 *
 * @author 
 */
@DS("sys")
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {
    /**
     * 查询菜单使用数量
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    public int checkMenuExistRole(Long menuId);

    /**
     * 通过角色ID删除角色和菜单关联
     *
     * @param roleId 角色ID
     * @return 结果
     */
    public int deleteRoleMenuByRoleId(Long roleId);

    /**
     * 批量删除角色菜单关联信息
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteRoleMenu(Long[] ids);

    /**
     * 批量新增角色菜单信息
     *
     * @param roleMenuList 角色菜单列表
     * @return 结果
     */
    public int batchRoleMenu(List<SysRoleMenuDto> roleMenuList);

    /**
     * 根据角色ID查询关联的菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    public List<Long> selectMenuIdsByRoleId(Long roleId);
}
