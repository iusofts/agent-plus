package com.iusofts.agentplus.web.system.controller;

import com.iusofts.agentplus.basic.web.annotation.OperationLogExclude;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.basic.utils.StringUtils;
import com.iusofts.agentplus.basic.web.annotation.Permission;
import com.iusofts.agentplus.system.dto.SysDictDataDto;
import com.iusofts.agentplus.system.interfaces.ISysDictDataService;
import com.iusofts.agentplus.system.interfaces.ISysDictTypeService;
import com.iusofts.agentplus.web.common.controller.BApiController;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.iusofts.agentplus.basic.enums.OperationLogExcludeTypeEnums.RES;
import static com.iusofts.agentplus.web.common.util.SessionUtil.getUsername;

@RestController
@RequestMapping("/bapi/system/dict/data")
public class SysDictDataController extends BApiController {
    @Autowired
    private ISysDictDataService dictDataService;

    @Autowired
    private ISysDictTypeService dictTypeService;

    @Permission("system:dict:query")
    @OperationLogExclude(type = RES)
    @Operation(summary = "获取字典数据列表")
    @PostMapping("/list")
    public PageResult<SysDictDataDto> list(@RequestBody SysDictDataDto dictData) {
        return dictDataService.selectDictDataList(dictData);
    }

    @Operation(summary = "查询字典数据详细")
    @GetMapping(value = "/{dictCode}")
    public SysDictDataDto getInfo(@PathVariable Long dictCode) {
        return (dictDataService.selectDictDataById(dictCode));
    }

    @Operation(summary = "根据字典类型查询字典数据信息")
    @GetMapping(value = "/type/{dictType}")
    public List<SysDictDataDto> dictType(@PathVariable String dictType) {
        List<SysDictDataDto> data = dictTypeService.selectDictDataByType(dictType);
        if (StringUtils.isNull(data)) {
            data = new ArrayList<>();
        }
        return (data);
    }

    @Permission("system:dict:add")
    @Operation(summary = "新增字典数据")
    @PostMapping
    public int add(@Validated @RequestBody SysDictDataDto dict) {
        dict.setCreateBy(getUsername());
        return (dictDataService.insertDictData(dict));
    }

    @Permission("system:dict:edit")
    @Operation(summary = "修改保存字典数据")
    @PutMapping
    public int edit(@Validated @RequestBody SysDictDataDto dict) {
        dict.setUpdateBy(getUsername());
        return (dictDataService.updateDictData(dict));
    }

    @Permission("system:dict:remove")
    @Operation(summary = "删除字典数据")
    @DeleteMapping("/{dictCodes}")
    public void remove(@PathVariable Long[] dictCodes) {
        dictDataService.deleteDictDataByIds(dictCodes);
    }
}
