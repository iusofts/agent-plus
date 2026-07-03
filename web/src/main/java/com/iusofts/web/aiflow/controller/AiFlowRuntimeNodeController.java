package com.iusofts.web.aiflow.controller;

import com.iusofts.aiflow.Interfaces.IAiFlowRuntimeNodeService;
import com.iusofts.aiflow.vo.*;
import com.iusofts.basic.annotation.OperationLogExclude;
import com.iusofts.common.vo.IdReqVo;
import com.iusofts.web.common.controller.BApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.iusofts.basic.enums.OperationLogExcludeTypeEnums.RES;

/**
 * <p>
 * 运行节点明细 前端控制器
 * </p>
 *
 * @author Ivan
 * @since 2026-06-12
 */
@Tag(name = "运行节点明细")
@RestController
@RequestMapping("/bapi/aiFlowRuntimeNode")
public class AiFlowRuntimeNodeController extends BApiController {

    @Resource
    private IAiFlowRuntimeNodeService aiFlowRuntimeNodeService;

    @Operation(description = "根据运行实例ID查询节点列表")
    @OperationLogExclude(type = RES)
    @PostMapping("/queryByRuntimeId")
    public List<AiFlowRuntimeNodeVo> queryByRuntimeId(@RequestBody AiFlowRuntimeNodeQueryReqVo reqVo) {
        return aiFlowRuntimeNodeService.queryByRuntimeId(reqVo);
    }

    @Operation(description = "根据ID查询节点详情")
    @PostMapping("/getById")
    public AiFlowRuntimeNodeDetailVo getById(@RequestBody IdReqVo reqVo) {
        return aiFlowRuntimeNodeService.getById(reqVo);
    }

}
