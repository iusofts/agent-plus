package com.iusofts.agentplus.web.chat.controller;

import com.iusofts.agentplus.chat.interfaces.IAiConversationService;
import com.iusofts.agentplus.chat.vo.conversation.AiConversationInfoVo;
import com.iusofts.agentplus.chat.vo.conversation.AiConversationQueryPageReqVo;
import com.iusofts.agentplus.chat.vo.conversation.AiConversationUpdateTitleReqVo;
import com.iusofts.agentplus.chat.vo.conversation.AiConversationVo;
import com.iusofts.agentplus.basic.web.annotation.OperationLogExclude;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.common.vo.IdReqVo;
import com.iusofts.agentplus.web.common.controller.BApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.iusofts.agentplus.basic.enums.OperationLogExcludeTypeEnums.RES;
import static com.iusofts.agentplus.common.constants.SysConstant.SYSCODE;

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
        reqVo.setOrgId(SYSCODE);
        return aiConversationService.queryPage(reqVo);
    }

    @Operation(description = "删除ai对话会话")
    @PostMapping("/remove")
    public void remove(@RequestBody IdReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        aiConversationService.remove(reqVo);
    }

    @Operation(description = "更新对话会话标题")
    @PostMapping("/updateTitle")
    public void updateTitle(@RequestBody AiConversationUpdateTitleReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        aiConversationService.updateTitle(reqVo.getId(), reqVo.getTitle(), reqVo.getOrgId(), reqVo.getOperatorId());
    }

    @Operation(description = "查询会话详情")
    @PostMapping("/getInfo")
    public AiConversationInfoVo getInfo(@RequestBody IdReqVo reqVo) {
        reqVo.setOrgId(SYSCODE);
        return aiConversationService.getInfo(reqVo);
    }

}