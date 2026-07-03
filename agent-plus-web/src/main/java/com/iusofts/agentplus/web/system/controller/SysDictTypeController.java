package com.iusofts.agentplus.web.system.controller;

import com.iusofts.agentplus.basic.annotation.OperationLogExclude;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.page.PageResult;
import com.iusofts.agentplus.basic.web.annotation.Permission;
import com.iusofts.agentplus.system.dto.SysDictTypeDto;
import com.iusofts.agentplus.system.interfaces.ISysDictTypeService;
import com.iusofts.agentplus.web.common.controller.BApiController;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.iusofts.agentplus.basic.enums.OperationLogExcludeTypeEnums.RES;
import static com.iusofts.agentplus.web.common.util.SessionUtil.getUsername;

@RestController
@RequestMapping("/bapi/system/dict/type")
public class SysDictTypeController extends BApiController {
    @Autowired
    private ISysDictTypeService dictTypeService;

    @Permission("system:dict:query")
    @OperationLogExclude(type = RES)
    @Operation(summary = "获取字典类型列表")
    @PostMapping("/list")
    public PageResult<SysDictTypeDto> list(@RequestBody SysDictTypeDto dictType) {
        return dictTypeService.selectDictTypeList(dictType);
    }

    @Operation(summary = "查询字典类型详细")
    @GetMapping(value = "/{dictId}")
    public SysDictTypeDto getInfo(@PathVariable Long dictId) {
        return (dictTypeService.selectDictTypeById(dictId));
    }

    @Permission("system:dict:add")
    @Operation(summary = "新增字典类型")
    @PostMapping
    public int add(@Validated @RequestBody SysDictTypeDto dict) {
        if (!dictTypeService.checkDictTypeUnique(dict)) {
            throw new SystemBusinessException("新增字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        dict.setCreateBy(getUsername());
        return (dictTypeService.insertDictType(dict));
    }

    @Permission("system:dict:edit")
    @Operation(summary = "修改字典类型")
    @PutMapping
    public int edit(@Validated @RequestBody SysDictTypeDto dict) {
        if (!dictTypeService.checkDictTypeUnique(dict)) {
            throw new SystemBusinessException("修改字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        dict.setUpdateBy(getUsername());
        return (dictTypeService.updateDictType(dict));
    }

    @Permission("system:dict:remove")
    @Operation(summary = "删除字典类型")
    @DeleteMapping("/{dictIds}")
    public void remove(@PathVariable Long[] dictIds) {
        dictTypeService.deleteDictTypeByIds(dictIds);
    }

    @Permission("system:dict:remove")
    @Operation(summary = "刷新字典缓存")
    @DeleteMapping("/refreshCache")
    public void refreshCache() {
        dictTypeService.resetDictCache();
    }

    @Operation(summary = "获取字典选择框列表")
    @GetMapping("/optionselect")
    public List<SysDictTypeDto> optionselect() {
        List<SysDictTypeDto> dictTypes = dictTypeService.selectDictTypeAll();
        return (dictTypes);
    }
}
