package com.iusofts.agentplus.web.library.controller;

import com.iusofts.agentplus.library.interfaces.IAiKnowledgeBaseService;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeBaseAddReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeBaseDetailVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeBaseEditReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeBaseQueryPageReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeBaseVo;
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

import static com.iusofts.agentplus.basic.enums.OperationLogExcludeTypeEnums.RES;
import static com.iusofts.agentplus.common.constants.SysConstant.SYSCODE;

/**
 * <p>
 * AI知识库 前端控制器
 * </p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
@Tag(name = "AI知识库")
@RestController
@RequestMapping("/bapi/ai/knowledgeBase")
public class AiKnowledgeBaseController extends BApiController {

    @Resource
    private IAiKnowledgeBaseService aiKnowledgeBaseService;

    @Operation(description = "新增知识库")
    @PostMapping("/add")
    public Long add(@Valid @RequestBody AiKnowledgeBaseAddReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOrgId(SYSCODE);
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        return aiKnowledgeBaseService.add(reqVo);
    }

    @Operation(description = "编辑知识库")
    @PostMapping("/edit")
    public void edit(@Valid @RequestBody AiKnowledgeBaseEditReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOrgId(SYSCODE);
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        aiKnowledgeBaseService.edit(reqVo);
    }

    @Operation(description = "分页查询知识库")
    @OperationLogExclude(type = RES)
    @PostMapping("/queryPage")
    public PageResult<AiKnowledgeBaseVo> queryPage(@RequestBody AiKnowledgeBaseQueryPageReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        return aiKnowledgeBaseService.queryPage(reqVo);
    }

    @Operation(description = "删除知识库")
    @PostMapping("/remove")
    public void remove(@RequestBody IdReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        aiKnowledgeBaseService.remove(reqVo);
    }

    @Operation(description = "根据ID查询")
    @PostMapping("/getById")
    public AiKnowledgeBaseDetailVo getById(@RequestBody IdReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        return aiKnowledgeBaseService.getById(reqVo);
    }

    @Operation(description = "重建知识库下所有文档的向量")
    @PostMapping("/rebuildAllVectors")
    public void rebuildAllVectors(@RequestBody IdReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOrgId(SYSCODE);
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        aiKnowledgeBaseService.rebuildAllVectors(reqVo);
    }

}
