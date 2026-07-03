package com.iusofts.web.system.controller;

import com.iusofts.basic.annotation.OperationLogExclude;
import com.iusofts.basic.dto.StringDto;
import com.iusofts.basic.exception.SystemBusinessException;
import com.iusofts.basic.page.PageResult;
import com.iusofts.basic.web.annotation.Permission;
import com.iusofts.system.dto.SysConfigDto;
import com.iusofts.system.interfaces.ISysConfigService;
import com.iusofts.web.common.controller.BApiController;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.iusofts.basic.enums.OperationLogExcludeTypeEnums.RES;
import static com.iusofts.web.common.util.SessionUtil.getUsername;

@RestController
@RequestMapping("/bapi/system/config")
public class SysConfigController extends BApiController {
    @Autowired
    private ISysConfigService configService;

    @Permission("system:config:query")
    @OperationLogExclude(type = RES)
    @Operation(summary = "获取参数配置列表")
    @PostMapping("/list")
    public PageResult<SysConfigDto> list(@RequestBody SysConfigDto config) {
        return configService.selectConfigList(config);
    }

    @Operation(summary = "根据参数编号获取详细信息")
    @GetMapping(value = "/{configId}")
    public SysConfigDto getInfo(@PathVariable Long configId) {
        return configService.selectConfigById(configId);
    }

    @Operation(summary = "根据参数键名查询参数值")
    @GetMapping(value = "/configKey/{configKey}")
    public StringDto getConfigKey(@PathVariable String configKey) {
        return new StringDto(configService.selectConfigByKey(configKey));
    }

    @Permission("system:config:add")
    @Operation(summary = "新增参数配置")
    @PostMapping
    public int add(@Validated @RequestBody SysConfigDto config) {
        if (!configService.checkConfigKeyUnique(config)) {
            throw new SystemBusinessException("新增参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        config.setCreateBy(getUsername());
        return configService.insertConfig(config);
    }

    @Permission("system:config:edit")
    @Operation(summary = "修改参数配置")
    @PutMapping
    public int edit(@Validated @RequestBody SysConfigDto config) {
        if (!configService.checkConfigKeyUnique(config)) {
            throw new SystemBusinessException("修改参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        config.setUpdateBy(getUsername());
        return configService.updateConfig(config);
    }

    @Permission("system:config:remove")
    @Operation(summary = "删除参数配置")
    @DeleteMapping("/{configIds}")
    public void remove(@PathVariable Long[] configIds) {
        configService.deleteConfigByIds(configIds);
    }

    @Permission("system:config:remove")
    @Operation(summary = "刷新参数缓存")
    @DeleteMapping("/refreshCache")
    public void refreshCache() {
        configService.resetConfigCache();
    }
}
