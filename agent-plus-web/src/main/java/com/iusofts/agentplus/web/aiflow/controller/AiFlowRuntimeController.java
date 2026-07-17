package com.iusofts.agentplus.web.aiflow.controller;

import com.iusofts.agentplus.aiflow.interfaces.IAiFlowRuntimeService;
import com.iusofts.agentplus.aiflow.vo.*;
import com.iusofts.agentplus.basic.web.annotation.BLoginUser;
import com.iusofts.agentplus.basic.web.annotation.OperationLogExclude;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
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

/**
 * <p>
 * 流程运行实例 前端控制器
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Tag(name = "流程运行实例")
@RestController
@RequestMapping("/bapi/ai/flowRuntime")
public class AiFlowRuntimeController extends BApiController {

    @Resource
    private IAiFlowRuntimeService aiFlowRuntimeService;

    @Operation(description = "新增流程运行实例（启动流程）")
    @PostMapping("/add")
    public void add(@RequestBody AiFlowRuntimeAddReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        aiFlowRuntimeService.add(reqVo);
    }

    @Operation(description = "分页查询流程运行实例")
    @OperationLogExclude(type = RES)
    @PostMapping("/queryPage")
    public PageResult<AiFlowRuntimeVo> queryPage(@RequestBody AiFlowRuntimeQueryPageReqVo reqVo) {
        return aiFlowRuntimeService.queryPage(reqVo);
    }

    @Operation(description = "根据流程ID查询运行实例列表")
    @OperationLogExclude(type = RES)
    @PostMapping("/queryByFlowId")
    public java.util.List<AiFlowRuntimeVo> queryByFlowId(@RequestBody AiFlowRuntimeQueryPageReqVo reqVo) {
        return aiFlowRuntimeService.queryByFlowId(reqVo.getFlowId());
    }

    @Operation(description = "删除流程运行实例")
    @PostMapping("/remove")
    public void remove(@RequestBody IdReqVo reqVo) {
        aiFlowRuntimeService.remove(reqVo);
    }

    @Operation(description = "根据ID查询流程运行实例详情")
    @PostMapping("/getById")
    public AiFlowRuntimeDetailVo getById(@RequestBody IdReqVo reqVo) {
        return aiFlowRuntimeService.getById(reqVo);
    }

    @Operation(description = "终止流程运行实例")
    @PostMapping("/terminate")
    public void terminate(@RequestBody AiFlowRuntimeTerminateReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        aiFlowRuntimeService.terminate(reqVo);
    }

}
