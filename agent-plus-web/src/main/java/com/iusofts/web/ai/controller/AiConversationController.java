package com.iusofts.web.ai.controller;

import com.iusofts.ai.interfaces.IAiConversationService;
import com.iusofts.ai.vo.conversation.AiConversationTestInfoVo;
import com.iusofts.ai.vo.conversation.AiConversationQueryPageReqVo;
import com.iusofts.ai.vo.conversation.AiConversationUpdateTitleReqVo;
import com.iusofts.ai.vo.conversation.AiConversationVo;
import com.iusofts.basic.annotation.OperationLogExclude;
import com.iusofts.basic.page.PageResult;
import com.iusofts.common.vo.IdReqVo;
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
 * ai对话会话 前端控制器
 * </p>
 *
 * @author Ivan
 * @since 2026-03-31
 */
@Tag(name = "ai对话会话")
@RestController
@RequestMapping("/bapi/aiConversation")
public class AiConversationController extends BApiController {

    @Resource
    private IAiConversationService aiConversationService;

    @Operation(description = "分页查询ai对话会话")
    @OperationLogExclude(type = RES)
    @PostMapping("/queryPage")
    public PageResult<AiConversationVo> queryPage(@RequestBody AiConversationQueryPageReqVo reqVo) {
        reqVo.setOrgId(0);
        return aiConversationService.queryPage(reqVo);
    }

    @Operation(description = "删除ai对话会话")
    @PostMapping("/remove")
    public void remove(@RequestBody IdReqVo reqVo) {
        reqVo.setOrgId(0);
        aiConversationService.remove(reqVo);
    }

    @Operation(description = "更新对话会话标题")
    @PostMapping("/updateTitle")
    public void updateTitle(@RequestBody AiConversationUpdateTitleReqVo reqVo) {
        reqVo.setOrgId(0);
        aiConversationService.updateTitle(reqVo.getId(), reqVo.getTitle(), reqVo.getOrgId(), reqVo.getOperatorId());
    }

    @Operation(description = "查询会话详情")
    @PostMapping("/getInfo")
    public AiConversationTestInfoVo getInfo(@RequestBody IdReqVo reqVo) {
        reqVo.setOrgId(0);
        return aiConversationService.getInfo(reqVo);
    }

}