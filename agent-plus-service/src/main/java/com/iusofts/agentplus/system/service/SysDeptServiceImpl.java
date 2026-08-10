package com.iusofts.agentplus.system.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.agentplus.basic.constants.UserConstants;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.text.Convert;
import com.iusofts.agentplus.basic.utils.StringUtils;
import com.iusofts.agentplus.system.dao.SysDeptMapper;
import com.iusofts.agentplus.system.dto.SysDeptDto;
import com.iusofts.agentplus.system.dto.SysUserDto;
import com.iusofts.agentplus.system.dto.TreeSelectDto;
import com.iusofts.agentplus.system.entity.SysDept;
import com.iusofts.agentplus.system.interfaces.ISysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门管理 服务实现
 *
 * @author
 */
@DS("sys")
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {
    @Autowired
    private SysDeptMapper deptMapper;

    /**
     * 查询部门管理数据
     *
     * @param dept 部门信息
     * @return 部门信息集合
     */
    @Override
    public List<SysDeptDto> selectDeptList(SysDeptDto dept) {
        return deptMapper.selectDeptList(dept);
    }

    /**
     * 查询部门树结构信息
     *
     * @param dept 部门信息
     * @return 部门树信息集合
     */
    @Override
    public List<TreeSelectDto> selectDeptTreeList(SysDeptDto dept) {
        List<SysDeptDto> depts = selectDeptList(dept);
        return buildDeptTreeSelect(depts);
    }

    /**
     * 构建前端所需要树结构
     *
     * @param depts 部门列表
     * @return 树结构列表
     */
    @Override
    public List<SysDeptDto> buildDeptTree(List<SysDeptDto> depts) {
        List<SysDeptDto> returnList = new ArrayList<SysDeptDto>();
        List<Long> tempList = depts.stream().map(SysDeptDto::getDeptId).collect(Collectors.toList());
        for (SysDeptDto dept : depts) {
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(dept.getParentId())) {
                recursionFn(depts, dept);
                returnList.add(dept);
            }
        }
        if (returnList.isEmpty()) {
            returnList = depts;
        }
        return returnList;
    }

    /**
     * 构建前端所需要下拉树结构
     *
     * @param depts 部门列表
     * @return 下拉树结构列表
     */
    @Override
    public List<TreeSelectDto> buildDeptTreeSelect(List<SysDeptDto> depts) {
        List<SysDeptDto> deptTrees = buildDeptTree(depts);
        return deptTrees.stream().map(TreeSelectDto::new).collect(Collectors.toList());
    }

    /**
     * 根据角色ID查询部门树信息
     *
     * @param roleId 角色ID
     * @return 选中部门列表
     */
    @Override
    public List<Long> selectDeptListByRoleId(Long roleId) {
        return deptMapper.selectDeptListByRoleId(roleId, false);
    }

    /**
     * 根据部门ID查询信息
     *
     * @param deptId 部门ID
     * @return 部门信息
     */
    /**
     * 按主键查部门。
     *
     * <p>独立事务 + 只读 + {@code REQUIRES_NEW}:挂起外层事务,在 sys 库上单独开新事务跑只读查询,
     * 跨库调用失败/异常不会回滚调用方(典型场景:ai_log 写配置时反查部门名)。
     * 类级 {@link DS}("sys") 已指定数据源。</p>
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public SysDeptDto selectDeptById(Long deptId) {
        return deptMapper.selectDeptById(deptId);
    }

    /**
     * 根据ID查询所有子部门（正常状态）
     *
     * @param deptId 部门ID
     * @return 子部门数
     */
    @Override
    public int selectNormalChildrenDeptById(Long deptId) {
        return deptMapper.selectNormalChildrenDeptById(deptId);
    }

    /**
     * 是否存在子节点
     *
     * @param deptId 部门ID
     * @return 结果
     */
    @Override
    public boolean hasChildByDeptId(Long deptId) {
        int result = deptMapper.hasChildByDeptId(deptId);
        return result > 0;
    }

    /**
     * 查询部门是否存在用户
     *
     * @param deptId 部门ID
     * @return 结果 true 存在 false 不存在
     */
    @Override
    public boolean checkDeptExistUser(Long deptId) {
        int result = deptMapper.checkDeptExistUser(deptId);
        return result > 0;
    }

    /**
     * 校验部门名称是否唯一
     *
     * @param dept 部门信息
     * @return 结果
     */
    @Override
    public boolean checkDeptNameUnique(SysDeptDto dept) {
        Long deptId = StringUtils.isNull(dept.getDeptId()) ? -1L : dept.getDeptId();
        SysDeptDto info = deptMapper.checkDeptNameUnique(dept.getDeptName(), dept.getParentId());
        if (StringUtils.isNotNull(info) && info.getDeptId().longValue() != deptId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验部门是否有数据权限
     *
     * @param deptId 部门id
     */
    @Override
    public void checkDeptDataScope(Long deptId) {
        //if (!SysUser.isAdmin(SecurityUtils.getUserId()) && StringUtils.isNotNull(deptId))
        if (!SysUserDto.isAdmin(1L) && StringUtils.isNotNull(deptId)) {
            SysDeptDto dept = new SysDeptDto();
            dept.setDeptId(deptId);
            List<SysDeptDto> depts = selectDeptList(dept);
            if (StringUtils.isEmpty(depts)) {
                throw new SystemBusinessException("没有权限访问部门数据！");
            }
        }
    }

    /**
     * 新增保存部门信息
     *
     * @param dept 部门信息
     * @return 结果
     */
    @Override
    public int insertDept(SysDeptDto dept) {
        SysDeptDto info = deptMapper.selectDeptById(dept.getParentId());
        // 如果父节点不为正常状态,则不允许新增子节点
        if (!UserConstants.DEPT_NORMAL.equals(info.getStatus())) {
            throw new SystemBusinessException("部门停用，不允许新增");
        }
        dept.setAncestors(info.getAncestors() + "," + dept.getParentId());
        return deptMapper.insertDept(dept);
    }

    /**
     * 修改保存部门信息
     *
     * @param dept 部门信息
     * @return 结果
     */
    @Override
    public int updateDept(SysDeptDto dept) {
        SysDeptDto newParentDept = deptMapper.selectDeptById(dept.getParentId());
        SysDeptDto oldDept = deptMapper.selectDeptById(dept.getDeptId());
        if (StringUtils.isNotNull(newParentDept) && StringUtils.isNotNull(oldDept)) {
            String newAncestors = newParentDept.getAncestors() + "," + newParentDept.getDeptId();
            String oldAncestors = oldDept.getAncestors();
            dept.setAncestors(newAncestors);
            updateDeptChildren(dept.getDeptId(), newAncestors, oldAncestors);
        }
        int result = deptMapper.updateDept(dept);
        if (UserConstants.DEPT_NORMAL.equals(dept.getStatus()) && StringUtils.isNotEmpty(dept.getAncestors())
                && !StringUtils.equals("0", dept.getAncestors())) {
            // 如果该部门是启用状态，则启用该部门的所有上级部门
            updateParentDeptStatusNormal(dept);
        }
        return result;
    }

    /**
     * 修改该部门的父级部门状态
     *
     * @param dept 当前部门
     */
    private void updateParentDeptStatusNormal(SysDeptDto dept) {
        String ancestors = dept.getAncestors();
        Long[] deptIds = Convert.toLongArray(ancestors);
        deptMapper.updateDeptStatusNormal(deptIds);
    }

    /**
     * 修改子元素关系
     *
     * @param deptId       被修改的部门ID
     * @param newAncestors 新的父ID集合
     * @param oldAncestors 旧的父ID集合
     */
    public void updateDeptChildren(Long deptId, String newAncestors, String oldAncestors) {
        List<SysDeptDto> children = deptMapper.selectChildrenDeptById(deptId);
        for (SysDeptDto child : children) {
            child.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
        }
        if (children.size() > 0) {
            deptMapper.updateDeptChildren(children);
        }
    }

    /**
     * 删除部门管理信息
     *
     * @param deptId 部门ID
     * @return 结果
     */
    @Override
    public int deleteDeptById(Long deptId) {
        return deptMapper.deleteDeptById(deptId);
    }

    @Override
    public void updateDeptSort(String[] deptIds, String[] orderNums) {
        try {
            for (int i = 0; i < deptIds.length; i++) {
                SysDept dept = new SysDept();
                dept.setDeptId(Convert.toLong(deptIds[i]));
                dept.setOrderNum(Convert.toInt(orderNums[i]));
                deptMapper.updateDeptSort(dept);
            }
        } catch (Exception e) {
            throw new SystemBusinessException("保存排序异常，请联系管理员");
        }
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<SysDeptDto> list, SysDeptDto t) {
        // 得到子节点列表
        List<SysDeptDto> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysDeptDto tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<SysDeptDto> getChildList(List<SysDeptDto> list, SysDeptDto t) {
        List<SysDeptDto> tlist = new ArrayList<SysDeptDto>();
        Iterator<SysDeptDto> it = list.iterator();
        while (it.hasNext()) {
            SysDeptDto n = (SysDeptDto) it.next();
            if (StringUtils.isNotNull(n.getParentId()) && n.getParentId().longValue() == t.getDeptId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<SysDeptDto> list, SysDeptDto t) {
        return getChildList(list, t).size() > 0 ? true : false;
    }
}
