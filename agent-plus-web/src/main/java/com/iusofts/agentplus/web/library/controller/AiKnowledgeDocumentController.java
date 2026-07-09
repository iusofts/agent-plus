package com.iusofts.agentplus.web.library.controller;

import com.iusofts.agentplus.library.interfaces.IAiKnowledgeDocumentService;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeDocumentAddReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeDocumentBatchAddReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeDocumentQueryPageReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeDocumentStatusReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeDocumentVo;
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
 * AI知识库文档 前端控制器
 * </p>
 *
 * <p>文件先由前端上传阿里云 OSS,再把 url + 文件名提交到此登记元数据。</p>
 *
 * @author Ivan
 * @since 2026-07-08
 */
@Tag(name = "AI知识库文档")
@RestController
@RequestMapping("/bapi/aiKnowledgeDocument")
public class AiKnowledgeDocumentController extends BApiController {

    @Resource
    private IAiKnowledgeDocumentService aiKnowledgeDocumentService;

    @Operation(description = "登记单个文档(OSS url + 文件名)")
    @PostMapping("/add")
    public Long add(@Valid @RequestBody AiKnowledgeDocumentAddReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOrgId(SYSCODE);
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        return aiKnowledgeDocumentService.add(reqVo);
    }

    @Operation(description = "批量登记文档")
    @PostMapping("/batchAdd")
    public void batchAdd(@Valid @RequestBody AiKnowledgeDocumentBatchAddReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOrgId(SYSCODE);
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        aiKnowledgeDocumentService.batchAdd(reqVo);
    }

    @Operation(description = "分页查询文档")
    @OperationLogExclude(type = RES)
    @PostMapping("/queryPage")
    public PageResult<AiKnowledgeDocumentVo> queryPage(@Valid @RequestBody AiKnowledgeDocumentQueryPageReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        return aiKnowledgeDocumentService.queryPage(reqVo);
    }

    @Operation(description = "删除文档")
    @PostMapping("/remove")
    public void remove(@RequestBody IdReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        aiKnowledgeDocumentService.remove(reqVo);
    }

    @Operation(description = "变更文档状态(可用/已禁用/已归档,联动分块与向量)")
    @PostMapping("/changeStatus")
    public void changeStatus(@Valid @RequestBody AiKnowledgeDocumentStatusReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOrgId(SYSCODE);
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        aiKnowledgeDocumentService.changeStatus(reqVo);
    }

    @Operation(description = "根据ID查询")
    @PostMapping("/getById")
    public AiKnowledgeDocumentVo getById(@RequestBody IdReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        return aiKnowledgeDocumentService.getById(reqVo);
    }

}
