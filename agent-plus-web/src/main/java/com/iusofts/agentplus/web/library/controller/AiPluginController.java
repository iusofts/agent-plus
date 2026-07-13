package com.iusofts.agentplus.web.library.controller;

import com.iusofts.agentplus.basic.web.annotation.OperationLogExclude;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.common.vo.IdReqVo;
import com.iusofts.agentplus.library.interfaces.IAiPluginService;
import com.iusofts.agentplus.library.vo.plugin.AiPluginAddReqVo;
import com.iusofts.agentplus.library.vo.plugin.AiPluginDetailVo;
import com.iusofts.agentplus.library.vo.plugin.AiPluginEditReqVo;
import com.iusofts.agentplus.library.vo.plugin.AiPluginQueryPageReqVo;
import com.iusofts.agentplus.library.vo.plugin.AiPluginVo;
import com.iusofts.agentplus.web.common.controller.BApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.iusofts.agentplus.basic.enums.OperationLogExcludeTypeEnums.RES;
import static com.iusofts.agentplus.common.constants.SysConstant.SYSCODE;

/**
 * <p>
 * ai插件 前端控制器
 * </p>
 *
 * @author Ivan
 * @since 2026-07-13
 */
@Tag(name = "ai插件")
@RestController
@RequestMapping("/bapi/ai/plugin")
public class AiPluginController extends BApiController {

    @Resource
    private IAiPluginService aiPluginService;

    @Operation(description = "新增ai插件")
    @PostMapping("/add")
    public void add(@RequestBody AiPluginAddReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        aiPluginService.add(reqVo);
    }

    @Operation(description = "编辑ai插件")
    @PostMapping("/edit")
    public void edit(@RequestBody AiPluginEditReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        aiPluginService.edit(reqVo);
    }

    @Operation(description = "分页查询ai插件")
    @OperationLogExclude(type = RES)
    @PostMapping("/queryPage")
    public PageResult<AiPluginVo> queryPage(@RequestBody AiPluginQueryPageReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        return aiPluginService.queryPage(reqVo);
    }

    @Operation(description = "删除ai插件")
    @PostMapping("/remove")
    public void remove(@RequestBody IdReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        aiPluginService.remove(reqVo);
    }

    @Operation(description = "根据ID查询")
    @PostMapping("/getById")
    public AiPluginDetailVo getById(@RequestBody IdReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        return aiPluginService.getById(reqVo);
    }

}
