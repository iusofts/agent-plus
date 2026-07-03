/*
 * Copyright (C) 2019 All rights reserved
 * Author: Ivan Shen
 * Date: 2019/9/5
 * Description:StsService.java
 */
package com.iusofts.agentplus.basic.oss;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.springframework.stereotype.Service;

/**
 * @author Ivan Shen
 */
@Service
public class StsService {

    /**
     * 赋予角色
     *
     * @param accessKeyId
     * @param accessKeySecret
     * @param roleArn
     * @param roleSessionName
     * @param policy
     * @param seconds
     * @return
     **/
    public AssumeRoleResponse assumeRole(
            String regionId,
            String accessKeyId,
            String accessKeySecret,
            String roleArn,
            String roleSessionName,
            String policy,
            Long seconds) {
        try {
            // 创建一个 Aliyun Acs Client, 用于发起 OpenAPI 请求
            IClientProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
            DefaultAcsClient client = new DefaultAcsClient(profile);
            // 创建一个 AssumeRoleRequest 并设置请求参数
            final AssumeRoleRequest request = new AssumeRoleRequest();
            //POST请求
            request.setMethod(MethodType.POST);
            //https协议
            request.setProtocol(ProtocolType.HTTPS);
            //持续时间
            request.setDurationSeconds(seconds);
            //角色id
            request.setRoleArn(roleArn);
            //应用程序标识(自己定义)
            request.setRoleSessionName(roleSessionName);
            //在赋予角色的同时,还赋予其他的权限策略
            request.setPolicy(policy);
            // 发起请求，并得到response
            final AssumeRoleResponse response = client.getAcsResponse(request);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
