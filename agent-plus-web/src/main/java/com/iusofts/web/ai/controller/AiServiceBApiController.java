package com.iusofts.web.ai.controller;

import com.alibaba.dashscope.common.Role;
import com.iusofts.ai.interfaces.IAiServiceInterface;
import com.iusofts.ai.vo.service.AiMessageVo;
import com.iusofts.ai.vo.service.AiServiceChatReqVo;
import com.iusofts.ai.vo.service.AiServiceChatReqVo.Message;
import com.iusofts.ai.vo.service.AiServiceChatTestReqVo;
import com.iusofts.basic.annotation.BLoginUser;
import com.iusofts.basic.validation.YzValidated;
import com.iusofts.system.vo.BLoginUserVo;
import com.iusofts.web.common.controller.BApiController;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RequestMapping("/bapi/aiService")
@RestController
public class AiServiceBApiController extends BApiController {

    @Resource
    private IAiServiceInterface aiService;

    @Operation(description = "发送聊天消息")
    @PostMapping("/chat")
    public AiMessageVo chat(@YzValidated @RequestBody AiServiceChatTestReqVo reqVo, @BLoginUser BLoginUserVo loginUserVo) {
        AiServiceChatReqVo chatReqVo = new AiServiceChatReqVo();
        chatReqVo.setConversationId(reqVo.getConversationId());
        chatReqVo.setMessages(Arrays.asList(new Message(Role.USER.getValue(), reqVo.getContent())));
        chatReqVo.setAgentId(reqVo.getAgentId());
        chatReqVo.setBusinessType(0);
        chatReqVo.setBusinessID("");
        chatReqVo.setOrgId(0);
        chatReqVo.setOperatorId(loginUserVo.getUser().getUserId());
        return aiService.chat(chatReqVo);
    }

}