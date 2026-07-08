package com.iusofts.agentplus.web.basics.controller;

import com.iusofts.agentplus.basic.web.annotation.BLoginUser;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.basics.interfaces.IIndustryService;
import com.iusofts.agentplus.basics.vo.IndustryAddReqVo;
import com.iusofts.agentplus.basics.vo.IndustryChangeStatusVo;
import com.iusofts.agentplus.basics.vo.IndustryEditVo;
import com.iusofts.agentplus.basics.vo.IndustryQueryPageReqVo;
import com.iusofts.agentplus.basics.vo.IndustryVo;
import com.iusofts.agentplus.common.vo.IdReqVo;
import com.iusofts.agentplus.system.vo.BLoginUserVo;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bapi/industry")
public class IndustryBApiController {
    @Resource
    private IIndustryService industryService;

    @Operation(summary = "新增行业")
    @PostMapping("/add")
    public void add(@RequestBody IndustryAddReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        industryService.add(reqVo);
    }

    @Operation(summary = "查询列表")
    @PostMapping("/queryPage")
    public PageResult<IndustryVo> queryPage(@RequestBody IndustryQueryPageReqVo reqVo) {
        return industryService.queryPage(reqVo);
    }

    @Operation(summary = "编辑")
    @PostMapping("/edit")
    public void edit(@RequestBody IndustryEditVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        industryService.edit(reqVo);
    }

    @Operation(summary = "启用/停用")
    @PostMapping("/changeStatus")
    public void changeStatus(@RequestBody IndustryChangeStatusVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        industryService.changeStatus(reqVo);
    }

    @Operation(summary = "删除")
    @PostMapping("/deleteById")
    public void deleteById(@RequestBody IdReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        industryService.deleteById(reqVo);
    }
}
