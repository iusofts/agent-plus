package com.iusofts.agentplus.web.ai.controller;

import com.iusofts.agentplus.ai.interfaces.IAiAgentService;
import com.iusofts.agentplus.ai.vo.*;
import com.iusofts.agentplus.basic.annotation.BLoginUser;
import com.iusofts.agentplus.basic.annotation.OperationLogExclude;
import com.iusofts.agentplus.basic.page.PageResult;
import com.iusofts.agentplus.common.vo.IdReqVo;
import com.iusofts.agentplus.system.vo.BLoginUserVo;
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
 * ai智能体 前端控制器
 * </p>
 *
 * @author Ivan
 * @since 2026-03-31
 */
@Tag(name = "ai智能体")
@RestController
@RequestMapping("/bapi/aiAgent")
public class AiAgentController extends BApiController {

    @Resource
    private IAiAgentService aiAgentService;

    @Operation(description = "新增ai智能体")
    @PostMapping("/add")
    public void add(@RequestBody AiAgentAddReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        aiAgentService.add(reqVo);
    }

    @Operation(description = "编辑ai智能体")
    @PostMapping("/edit")
    public void edit(@RequestBody AiAgentEditReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        aiAgentService.edit(reqVo);
    }

    @Operation(description = "分页查询ai智能体")
    @OperationLogExclude(type = RES)
    @PostMapping("/queryPage")
    public PageResult<AiAgentVo> queryPage(@RequestBody AiAgentQueryPageReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        return aiAgentService.queryPage(reqVo);
    }
    
    @Operation(description = "删除ai智能体")
    @PostMapping("/remove")
    public void remove(@RequestBody IdReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        aiAgentService.remove(reqVo);
    }

    @Operation(description = "根据ID查询")
    @PostMapping("/getById")
    public AiAgentDetailVo getById(@RequestBody IdReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        return aiAgentService.getById(reqVo);
    }

    @Operation(description = "设置为默认智能体")
    @PostMapping("/setDefault")
    public void setDefault(@RequestBody AiAgentSetDefaultReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOrgId(SYSCODE);
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        aiAgentService.setDefault(reqVo);
    }

    @Operation(description = "设置为系统预制智能体")
    @PostMapping("/setSystem")
    public void setSystem(@RequestBody AiAgentSetSystemReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOrgId(SYSCODE);
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        aiAgentService.setSystem(reqVo);
    }

}