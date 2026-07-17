package com.iusofts.agentplus.web.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iusofts.agentplus.basic.web.annotation.OriginResponseBody;
import com.iusofts.agentplus.basic.exception.AbstractBusinessException;
import com.iusofts.agentplus.basic.exception.ExceptionTypeEnum;
import com.iusofts.agentplus.basic.web.response.BasicResponse;
import com.iusofts.agentplus.basic.web.response.BasicResponseEnums;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.List;

@ControllerAdvice(basePackages = {"com.iusofts"})
public class ResponseHandler implements ResponseBodyAdvice<Object> {

    private final static Logger log = LoggerFactory.getLogger(ResponseHandler.class);

    @Autowired
    private ObjectMapper objectMapper;

    @ExceptionHandler(AbstractBusinessException.class)
    @ResponseBody
    public BasicResponse handleAbstractBusinessException(AbstractBusinessException ex) {
        log.warn(ex.getMessage(), ex);
        BasicResponse basicResponse = new BasicResponse();
        basicResponse.setErrorCode(ex.getCode());
        basicResponse.setSuccess(false);
        basicResponse.setMessage(ex.getMessage());
        basicResponse.setData(ex.getData());
        return basicResponse;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public BasicResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn(ex.getMessage(), ex);
        BasicResponse basicResponse = new BasicResponse();
        basicResponse.setErrorCode(BasicResponseEnums.FAIL.getCode());
        basicResponse.setSuccess(false);
        BindingResult bindingResult = ex.getBindingResult();
        List<ObjectError> allErrors = bindingResult.getAllErrors();
        String message = "";
        for (ObjectError error : allErrors) {
            String defaultMessage = error.getDefaultMessage();
            message = message + defaultMessage;
        }
        basicResponse.setMessage(message);
        return basicResponse;
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public BasicResponse handleException(Exception ex) {
        log.error(ex.getMessage(), ex);
        BasicResponse basicResponse = new BasicResponse();
        basicResponse.setErrorCode(BasicResponseEnums.FAIL.getCode());
        basicResponse.setSuccess(false);
        basicResponse.setMessage(ExceptionTypeEnum.UNKNOWN_EXCEPTION.getDetail());
        return basicResponse;
    }

    // ====================== 核心改造 1 ======================
    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        // 如果有 @OriginResponseBody 直接不包装
        if (methodParameter.hasMethodAnnotation(OriginResponseBody.class)) {
            return false;
        }
        return AbstractJackson2HttpMessageConverter.class.isAssignableFrom(aClass);
    }

    // ====================== 核心改造 2 ======================
    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        // 有注解：直接返回原样数据，不包装
        if (methodParameter.hasMethodAnnotation(OriginResponseBody.class)) {
            return o;
        }

        // 无注解：走原来的统一包装逻辑
        Object wrapperBody = o;
        if (!(o instanceof BasicResponse)) {
            BasicResponse basicResponse = new BasicResponse();
            basicResponse.setErrorCode(BasicResponseEnums.SUCCESS.getCode());
            basicResponse.setMessage(BasicResponseEnums.SUCCESS.getMessage());
            basicResponse.setSuccess(true);
            basicResponse.setData(o);
            if (o instanceof String) {
                try {
                    wrapperBody = objectMapper.writeValueAsString(basicResponse);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else {
                wrapperBody = basicResponse;
            }
        }
        return wrapperBody;
    }
}