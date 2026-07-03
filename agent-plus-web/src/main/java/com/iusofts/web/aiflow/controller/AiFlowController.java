package com.iusofts.web.aiflow.controller;

import com.iusofts.aiflow.interfaces.IAiFlowService;
import com.iusofts.aiflow.vo.*;
import com.iusofts.basic.annotation.BLoginUser;
import com.iusofts.basic.annotation.OperationLogExclude;
import com.iusofts.basic.page.PageResult;
import com.iusofts.common.vo.IdReqVo;
import com.iusofts.system.vo.BLoginUserVo;
import com.iusofts.web.common.controller.BApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.iusofts.basic.enums.OperationLogExcludeTypeEnums.RES;

/**
 * <p>
 * AI流程 前端控制器
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Tag(name = "AI流程")
@RestController
@RequestMapping("/bapi/aiFlow")
public class AiFlowController extends BApiController {

    @Resource
    private IAiFlowService aiFlowService;

    @Operation(description = "新增AI流程")
    @PostMapping("/add")
    public void add(@RequestBody AiFlowAddReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        aiFlowService.add(reqVo);
    }

    @Operation(description = "编辑AI流程")
    @PostMapping("/edit")
    public void edit(@RequestBody AiFlowEditReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        aiFlowService.edit(reqVo);
    }

    @Operation(description = "分页查询AI流程")
    @OperationLogExclude(type = RES)
    @PostMapping("/queryPage")
    public PageResult<AiFlowVo> queryPage(@RequestBody AiFlowQueryPageReqVo reqVo) {
        return aiFlowService.queryPage(reqVo);
    }

    @Operation(description = "查询所有启用的AI流程")
    @OperationLogExclude(type = RES)
    @PostMapping("/queryAll")
    public java.util.List<AiFlowVo> queryAll() {
        return aiFlowService.queryAll();
    }

    @Operation(description = "删除AI流程")
    @PostMapping("/remove")
    public void remove(@RequestBody IdReqVo reqVo) {
        aiFlowService.remove(reqVo);
    }

    @Operation(description = "根据ID查询AI流程详情")
    @PostMapping("/getById")
    public AiFlowDetailVo getById(@RequestBody IdReqVo reqVo) {
        return aiFlowService.getById(reqVo);
    }

    @Operation(description = "设置AI流程启用状态")
    @PostMapping("/setStatus")
    public void setStatus(@RequestBody AiFlowSetStatusReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        aiFlowService.setStatus(reqVo);
    }

}
