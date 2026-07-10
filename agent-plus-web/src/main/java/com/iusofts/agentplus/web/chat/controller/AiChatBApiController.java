package com.iusofts.agentplus.web.chat.controller;

import com.iusofts.agentplus.basic.validation.ApValidated;
import com.iusofts.agentplus.basic.web.annotation.BLoginUser;
import com.iusofts.agentplus.chat.interfaces.IAiChatServiceInterface;
import com.iusofts.agentplus.chat.vo.AiMessageVo;
import com.iusofts.agentplus.chat.vo.AiServiceChatReqVo;
import com.iusofts.agentplus.system.vo.BLoginUserVo;
import com.iusofts.agentplus.web.common.controller.BApiController;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.iusofts.agentplus.common.constants.SysConstant.SYSCODE;

@RequestMapping("/bapi/ai/chat")
@RestController
public class AiChatBApiController extends BApiController {

    @Resource
    private IAiChatServiceInterface aiService;

    @Operation(description = "发送聊天消息")
    @PostMapping("/sendMessage")
    public AiMessageVo sendMessage(@ApValidated @RequestBody AiServiceChatReqVo chatReqVo, @BLoginUser BLoginUserVo loginUserVo) {
        chatReqVo.setOrgId(SYSCODE);
        chatReqVo.setOperatorId(loginUserVo.getUser().getUserId());
        return aiService.chat(chatReqVo);
    }

}