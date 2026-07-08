package com.iusofts.agentplus.web.library.controller;

import com.iusofts.agentplus.library.interfaces.IAiModelService;
import com.iusofts.agentplus.library.vo.model.AiModelAddReqVo;
import com.iusofts.agentplus.library.vo.model.AiModelDetailVo;
import com.iusofts.agentplus.library.vo.model.AiModelEditReqVo;
import com.iusofts.agentplus.library.vo.model.AiModelQueryPageReqVo;
import com.iusofts.agentplus.library.vo.model.AiModelVo;
import com.iusofts.agentplus.basic.web.annotation.BLoginUser;
import com.iusofts.agentplus.basic.web.annotation.OperationLogExclude;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.common.vo.IdReqVo;
import com.iusofts.agentplus.system.vo.BLoginUserVo;
import com.iusofts.agentplus.web.common.controller.BApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.iusofts.agentplus.basic.enums.OperationLogExcludeTypeEnums.RES;
import static com.iusofts.agentplus.common.constants.SysConstant.SYSCODE;

/**
 * <p>
 * AI模型配置 前端控制器
 * </p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
@Tag(name = "AI模型配置")
@RestController
@RequestMapping("/bapi/aiModel")
public class AiModelController extends BApiController {

    @Resource
    private IAiModelService aiModelService;

    @Operation(description = "新增模型")
    @PostMapping("/add")
    public void add(@Valid @RequestBody AiModelAddReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOrgId(SYSCODE);
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        aiModelService.add(reqVo);
    }

    @Operation(description = "编辑模型")
    @PostMapping("/edit")
    public void edit(@Valid @RequestBody AiModelEditReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOrgId(SYSCODE);
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        aiModelService.edit(reqVo);
    }

    @Operation(description = "分页查询模型")
    @OperationLogExclude(type = RES)
    @PostMapping("/queryPage")
    public PageResult<AiModelVo> queryPage(@RequestBody AiModelQueryPageReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        return aiModelService.queryPage(reqVo);
    }

    @Operation(description = "查询启用的模型列表(下拉用)")
    @OperationLogExclude(type = RES)
    @PostMapping("/queryEnabled")
    public List<AiModelVo> queryEnabled(@RequestBody AiModelQueryPageReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        return aiModelService.queryEnabled(reqVo.getOrgId(), reqVo.getModelType());
    }

    @Operation(description = "删除模型")
    @PostMapping("/remove")
    public void remove(@RequestBody IdReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        aiModelService.remove(reqVo);
    }

    @Operation(description = "根据ID查询")
    @PostMapping("/getById")
    public AiModelDetailVo getById(@RequestBody IdReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        return aiModelService.getById(reqVo);
    }

}
