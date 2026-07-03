package com.iusofts.agentplus.web.common.controller;

import com.iusofts.agentplus.basic.annotation.BLoginUser;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.freemaker.TemplateManage;
import com.iusofts.agentplus.basic.validation.ApValidated;
import com.iusofts.agentplus.system.vo.BLoginUserVo;
import com.iusofts.agentplus.web.common.vo.TestVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@Tag(name = "测试")
public class TestController extends BApiController {

    @Autowired
    private TemplateManage templateManage;

    @Operation(description = "测试")
    @PostMapping("/test")
    public TestVO test(@ApValidated @RequestBody TestVO param) {
        return param;
    }

    @Operation(description = "测试Html")
    @GetMapping("/testHtml")
    public String testHtml() {
        return templateManage.parse("test", null);
    }

    @Operation(description = "测试异常")
    @PostMapping("/testE")
    public void test() {
        throw new SystemBusinessException("测试异常");
    }


    @Operation(description = "测试图形验证码")
    @GetMapping("/testCodes")
    public void testCodes(HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    @Operation(description = "测试")
    @PostMapping("/testLoginUser")
    public BLoginUserVo test(@BLoginUser BLoginUserVo bLoginUserVo) {
        return bLoginUserVo;
    }

}
