package com.iusofts.agentplus.web.aiflow.controller;

import com.iusofts.agentplus.aiflow.interfaces.IAiFlowTrialService;
import com.iusofts.agentplus.aiflow.vo.AiFlowTrialRunFlowReqVo;
import com.iusofts.agentplus.aiflow.vo.AiFlowTrialRunNodeReqVo;
import com.iusofts.agentplus.aiflow.vo.AiFlowTrialRunResultVo;
import com.iusofts.agentplus.basic.web.annotation.BLoginUser;
import com.iusofts.agentplus.basic.web.annotation.OperationLogExclude;
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
 * 流程试运行 前端控制器
 * </p>
 *
 * @author Ivan
 * @since 2026-07-16
 */
@Tag(name = "流程试运行")
@RestController
@RequestMapping("/bapi/ai/flowTrial")
public class AiFlowTrialController extends BApiController {

    @Resource
    private IAiFlowTrialService aiFlowTrialService;

    @Operation(description = "试运行整个流程")
    @OperationLogExclude(type = RES)
    @PostMapping("/runFlow")
    public AiFlowTrialRunResultVo runFlow(@RequestBody AiFlowTrialRunFlowReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        return aiFlowTrialService.runFlow(reqVo);
    }

    @Operation(description = "试运行单个节点")
    @OperationLogExclude(type = RES)
    @PostMapping("/runNode")
    public AiFlowTrialRunResultVo runNode(@RequestBody AiFlowTrialRunNodeReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        return aiFlowTrialService.runNode(reqVo);
    }

}
