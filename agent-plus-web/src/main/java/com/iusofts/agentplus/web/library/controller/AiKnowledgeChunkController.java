package com.iusofts.agentplus.web.library.controller;

import com.iusofts.agentplus.library.interfaces.IAiKnowledgeChunkService;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeChunkAddReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeChunkEditReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeChunkQueryPageReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeChunkStatusReqVo;
import com.iusofts.agentplus.library.vo.knowledge.AiKnowledgeChunkVo;
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
 * AI知识库文档分块 前端控制器
 * </p>
 *
 * @author Ivan
 * @since 2026-07-09
 */
@Tag(name = "AI知识库文档分块")
@RestController
@RequestMapping("/bapi/aiKnowledgeChunk")
public class AiKnowledgeChunkController extends BApiController {

    @Resource
    private IAiKnowledgeChunkService aiKnowledgeChunkService;

    @Operation(description = "手动新增分块(向量化并写入)")
    @PostMapping("/add")
    public Long add(@Valid @RequestBody AiKnowledgeChunkAddReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOrgId(SYSCODE);
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        return aiKnowledgeChunkService.add(reqVo);
    }

    @Operation(description = "分页查询分块")
    @OperationLogExclude(type = RES)
    @PostMapping("/queryPage")
    public PageResult<AiKnowledgeChunkVo> queryPage(@Valid @RequestBody AiKnowledgeChunkQueryPageReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        return aiKnowledgeChunkService.queryPage(reqVo);
    }

    @Operation(description = "根据ID查询分块详情")
    @PostMapping("/getById")
    public AiKnowledgeChunkVo getById(@RequestBody IdReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        return aiKnowledgeChunkService.getById(reqVo);
    }

    @Operation(description = "编辑分块内容(重新向量化)")
    @PostMapping("/edit")
    public void edit(@Valid @RequestBody AiKnowledgeChunkEditReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOrgId(SYSCODE);
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        aiKnowledgeChunkService.edit(reqVo);
    }

    @Operation(description = "启用/停用分块(重建或删除向量)")
    @PostMapping("/changeStatus")
    public void changeStatus(@Valid @RequestBody AiKnowledgeChunkStatusReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        reqVo.setOrgId(SYSCODE);
        reqVo.setOperatorId(loginUserVo.getUser().getUserId());
        aiKnowledgeChunkService.changeStatus(reqVo);
    }

    @Operation(description = "删除分块")
    @PostMapping("/remove")
    public void remove(@RequestBody IdReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        aiKnowledgeChunkService.remove(reqVo);
    }

}
