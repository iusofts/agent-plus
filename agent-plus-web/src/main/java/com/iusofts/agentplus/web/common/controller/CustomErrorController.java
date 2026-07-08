package com.iusofts.agentplus.web.common.controller;

import com.iusofts.agentplus.basic.web.response.BasicResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public BasicResponse handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        BasicResponse basicResponse = new BasicResponse();
        if (statusCode != null && statusCode == 404) {
            basicResponse.setErrorCode(404);
            basicResponse.setSuccess(false);
            basicResponse.setMessage("接口不存在");
        } else {
            basicResponse.setErrorCode(statusCode != null ? statusCode : 500);
            basicResponse.setSuccess(false);
            basicResponse.setMessage("服务繁忙");
        }
        return basicResponse;
    }
}
