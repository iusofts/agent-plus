package com.iusofts.agentplus.web.aiflow.controller;

import com.iusofts.agentplus.aiflow.interfaces.IAiFlowVersionService;
import com.iusofts.agentplus.aiflow.vo.*;
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

import java.util.List;

import static com.iusofts.agentplus.basic.enums.OperationLogExcludeTypeEnums.RES;

/**
 * <p>
 * AI流程版本 前端控制器
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Tag(name = "AI流程版本")
@RestController
@RequestMapping("/bapi/aiFlowVersion")
public class AiFlowVersionController extends BApiController {

    @Resource
    private IAiFlowVersionService aiFlowVersionService;

    @Operation(description = "保存AI工作流程版本（新增/编辑）")
    @PostMapping("/saveWorkflow")
    public void saveWorkflow(@RequestBody AiFlowVersionSaveReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        aiFlowVersionService.saveWorkflow(reqVo);
    }

    @Operation(description = "单独更新流程全局配置configJson")
    @PostMapping("/updateConfig")
    public void updateConfig(@RequestBody AiFlowVersionConfigUpdateReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        aiFlowVersionService.updateConfig(reqVo);
    }

    @Operation(description = "分页查询AI流程版本")
    @OperationLogExclude(type = RES)
    @PostMapping("/queryPage")
    public PageResult<AiFlowVersionVo> queryPage(@RequestBody AiFlowVersionQueryPageReqVo reqVo) {
        return aiFlowVersionService.queryPage(reqVo);
    }

    @Operation(description = "根据流程ID查询版本列表")
    @OperationLogExclude(type = RES)
    @PostMapping("/queryByFlowId")
    public List<AiFlowVersionVo> queryByFlowId(@RequestBody IdReqVo reqVo) {
        return aiFlowVersionService.queryByFlowId(reqVo.getId());
    }

    @Operation(description = "删除AI流程版本")
    @PostMapping("/remove")
    public void remove(@RequestBody IdReqVo reqVo) {
        aiFlowVersionService.remove(reqVo);
    }

    @Operation(description = "根据ID查询AI工作流程版本详情")
    @PostMapping("/getWorkflowById")
    public AiFlowVersionDetailVo getWorkflowById(@RequestBody IdReqVo reqVo) {
        return aiFlowVersionService.getById(reqVo);
    }

    @Operation(description = "发布AI流程版本")
    @PostMapping("/publish")
    public void publish(@RequestBody AiFlowVersionPublishReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        aiFlowVersionService.publish(reqVo);
    }

    @Operation(description = "查询工作流程编辑详情（根据最新版本状态返回）")
    @OperationLogExclude(type = RES)
    @PostMapping("/getWorkflowEditDetailByFlowId")
    public AiFlowVersionDetailVo getWorkflowEditDetailByFlowId(@RequestBody IdReqVo reqVo) {
        return aiFlowVersionService.getWorkflowEditDetailByFlowId(reqVo.getId());
    }

}
