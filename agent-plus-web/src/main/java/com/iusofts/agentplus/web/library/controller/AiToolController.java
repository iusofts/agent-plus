package com.iusofts.agentplus.web.library.controller;

import com.iusofts.agentplus.basic.web.annotation.OperationLogExclude;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.common.vo.IdReqVo;
import com.iusofts.agentplus.library.interfaces.IAiToolService;
import com.iusofts.agentplus.library.vo.tool.AiToolAddReqVo;
import com.iusofts.agentplus.library.vo.tool.AiToolDetailVo;
import com.iusofts.agentplus.library.vo.tool.AiToolEditReqVo;
import com.iusofts.agentplus.library.vo.tool.AiToolQueryPageReqVo;
import com.iusofts.agentplus.library.vo.tool.AiToolStatusReqVo;
import com.iusofts.agentplus.library.vo.tool.AiToolVo;
import com.iusofts.agentplus.web.common.controller.BApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.iusofts.agentplus.basic.enums.OperationLogExcludeTypeEnums.RES;
import static com.iusofts.agentplus.common.constants.SysConstant.SYSCODE;

/**
 * <p>
 * ai工具 前端控制器
 * </p>
 *
 * @author Ivan
 * @since 2026-07-12
 */
@Tag(name = "ai工具")
@RestController
@RequestMapping("/bapi/ai/tool")
public class AiToolController extends BApiController {

    @Resource
    private IAiToolService aiToolService;

    @Operation(description = "新增ai工具")
    @PostMapping("/add")
    public void add(@Valid @RequestBody AiToolAddReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        aiToolService.add(reqVo);
    }

    @Operation(description = "编辑ai工具")
    @PostMapping("/edit")
    public void edit(@Valid @RequestBody AiToolEditReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        aiToolService.edit(reqVo);
    }

    @Operation(description = "变更ai工具状态")
    @PostMapping("/changeStatus")
    public void changeStatus(@Valid @RequestBody AiToolStatusReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        aiToolService.changeStatus(reqVo);
    }

    @Operation(description = "分页查询ai工具")
    @OperationLogExclude(type = RES)
    @PostMapping("/queryPage")
    public PageResult<AiToolVo> queryPage(@RequestBody AiToolQueryPageReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        return aiToolService.queryPage(reqVo);
    }

    @Operation(description = "删除ai工具")
    @PostMapping("/remove")
    public void remove(@RequestBody IdReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        aiToolService.remove(reqVo);
    }

    @Operation(description = "根据ID查询")
    @PostMapping("/getById")
    public AiToolDetailVo getById(@RequestBody IdReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        return aiToolService.getById(reqVo);
    }

}
