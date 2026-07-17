package com.iusofts.agentplus.web.system.controller;

import com.iusofts.agentplus.basic.web.annotation.OperationLogExclude;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.utils.StringUtils;
import com.iusofts.agentplus.basic.constants.UserConstants;
import com.iusofts.agentplus.basic.web.annotation.Permission;
import com.iusofts.agentplus.system.dto.SysDeptDto;
import com.iusofts.agentplus.system.interfaces.ISysDeptService;
import com.iusofts.agentplus.web.common.controller.BApiController;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.iusofts.agentplus.basic.enums.OperationLogExcludeTypeEnums.RES;
import static com.iusofts.agentplus.web.common.util.SessionUtil.getUsername;

@RestController
@RequestMapping("/bapi/system/dept")
public class SysDeptController extends BApiController {
    @Autowired
    private ISysDeptService deptService;

    @Permission("system:dept:query")
    @OperationLogExclude(type = RES)
    @Operation(summary = "获取部门列表")
    @PostMapping("/list")
    public List<SysDeptDto> list(@RequestBody SysDeptDto dept) {
        List<SysDeptDto> depts = deptService.selectDeptList(dept);
        return depts;
    }

    @OperationLogExclude(type = RES)
    @Operation(summary = "查询部门列表（排除节点）")
    @GetMapping("/list/exclude/{deptId}")
    public List<SysDeptDto> excludeChild(@PathVariable(value = "deptId", required = false) Long deptId) {
        List<SysDeptDto> depts = deptService.selectDeptList(new SysDeptDto());
        depts.removeIf(d -> d.getDeptId().intValue() == deptId || ArrayUtils.contains(StringUtils.split(d.getAncestors(), ","), deptId + ""));
        return (depts);
    }

    @Operation(summary = "根据部门编号获取详细信息")
    @GetMapping(value = "/{deptId}")
    public SysDeptDto getInfo(@PathVariable Long deptId) {
        deptService.checkDeptDataScope(deptId);
        return (deptService.selectDeptById(deptId));
    }

    @Permission("system:dept:add")
    @Operation(summary = "新增部门")
    @PostMapping
    public int add(@Validated @RequestBody SysDeptDto dept) {
        if (!deptService.checkDeptNameUnique(dept)) {
            throw new SystemBusinessException("新增部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        dept.setCreateBy(getUsername());
        return (deptService.insertDept(dept));
    }

    @Permission("system:dept:edit")
    @Operation(summary = "修改部门")
    @PutMapping
    public int edit(@Validated @RequestBody SysDeptDto dept) {
        Long deptId = dept.getDeptId();
        deptService.checkDeptDataScope(deptId);
        if (!deptService.checkDeptNameUnique(dept)) {
            throw new SystemBusinessException("修改部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        } else if (dept.getParentId().equals(deptId)) {
            throw new SystemBusinessException("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己");
        } else if (StringUtils.equals(UserConstants.DEPT_DISABLE, dept.getStatus()) && deptService.selectNormalChildrenDeptById(deptId) > 0) {
            throw new SystemBusinessException("该部门包含未停用的子部门！");
        }
        dept.setUpdateBy(getUsername());
        return (deptService.updateDept(dept));
    }

    @Permission("system:dept:remove")
    @Operation(summary = "删除部门")
    @DeleteMapping("/{deptId}")
    public int remove(@PathVariable Long deptId) {
        if (deptService.hasChildByDeptId(deptId)) {
            throw new SystemBusinessException("存在下级部门,不允许删除");
        }
        if (deptService.checkDeptExistUser(deptId)) {
            throw new SystemBusinessException("部门存在用户,不允许删除");
        }
        deptService.checkDeptDataScope(deptId);
        return (deptService.deleteDeptById(deptId));
    }

    @Permission("system:dept:edit")
    @Operation(summary = "更新排序")
    @PutMapping("/updateSort")
    public void updateSort(@RequestBody Map<String, String> params) {
        String[] deptIds = params.get("deptIds").split(",");
        String[] orderNums = params.get("orderNums").split(",");
        deptService.updateDeptSort(deptIds, orderNums);
    }
}
