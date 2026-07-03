package com.iusofts.basic.sms;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ivan
 */
public class SMSUtil {

    /**
     * 产品名称:云通信短信API产品,开发者无需替换
     */
    static final String product = "Dysmsapi";
    /**
     * 产品域名,开发者无需替换
     */
    static final String domain = "dysmsapi.aliyuncs.com";

    static final String accessKeyId = "LTAI4Fj6ruNTrHNtfnKTxFck";
    static final String accessKeySecret = "CaZv27MRPT4eIhhZkd5DCQgTGika63";

    private static final Logger logger = LoggerFactory.getLogger(SMSUtil.class);

    public static CommonResponse sendSmsCode(String mobile, String code) throws ClientException {

        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        IAcsClient client = new DefaultAcsClient(profile);

        //组装请求对象-具体描述见控制台-文档部分内容
        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        //必填:待发送手机号
        request.putQueryParameter("PhoneNumbers", mobile);
        //必填:短信签名-可在短信控制台中找到
        request.putQueryParameter("SignName", "Rsun");
        //必填:短信模板-可在短信控制台中找到
        request.putQueryParameter("TemplateCode", "SMS_179065324");
        //验证码${code}，您正在登录家FUN系统，感谢您的支持！
        JSONObject json = new JSONObject();
        json.put("code", code);
        request.putQueryParameter("TemplateParam", json.toJSONString());
        return client.getCommonResponse(request);
    }

    public static CommonResponse sendSmsCode(String phone, String TemplateCode, JSONObject json) throws ClientException {
        logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"+accessKeyId);
        logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"+accessKeySecret);
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        IAcsClient client = new DefaultAcsClient(profile);

        //组装请求对象-具体描述见控制台-文档部分内容
        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        //必填:待发送手机号
        request.putQueryParameter("PhoneNumbers", phone);
        //必填:短信签名-可在短信控制台中找到
        request.putQueryParameter("SignName", "Rsun");
        //必填:短信模板-可在短信控制台中找到
        request.putQueryParameter("TemplateCode", TemplateCode);
        //替换文本内容
        request.putQueryParameter("TemplateParam", json.toJSONString());
        logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ok");
        CommonResponse response = client.getCommonResponse(request);
        logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ok1"+JSONObject.toJSON(response));
        return response;
    }

    public static CommonResponse sendRsunSms(String mobile, String templateCode, JSONObject json) throws ClientException {

        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        IAcsClient client = new DefaultAcsClient(profile);

        //组装请求对象-具体描述见控制台-文档部分内容
        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        //必填:待发送手机号
        request.putQueryParameter("PhoneNumbers", mobile);
        //必填:短信签名-可在短信控制台中找到
        request.putQueryParameter("SignName", "Rsun");
        //必填:短信模板-可在短信控制台中找到
        request.putQueryParameter("TemplateCode", templateCode);
        //验证码${code}，您正在登录家FUN系统，感谢您的支持！
        request.putQueryParameter("TemplateParam", json.toJSONString());
        return client.getCommonResponse(request);
    }
}
