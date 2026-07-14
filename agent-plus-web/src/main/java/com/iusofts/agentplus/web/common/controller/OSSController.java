/*
 * Copyright (C) 2019 All rights reserved
 * Author: Ivan Shen
 * Date: 2019/9/5
 * Description:OSSController.java
 */
package com.iusofts.agentplus.web.common.controller;

import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.oss.OSSProperties;
import com.iusofts.agentplus.basic.oss.StsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Ivan Shen
 */
@RestController
@Tag(name = "OSS")
@RequestMapping("/bapi/oss")
public class OSSController extends BApiController {

    @Autowired
    private OSSProperties ossProperties;
    @Autowired
    private StsService stsService;

    @Data
    public static class OssConfigResponse {
        private AssumeRoleResponse.Credentials credentials;
        private String domain;
        private String regionId;
        private String bucket;
    }

    /**
     * 签名授权
     *
     * @return
     **/
    @PostMapping("/getOssToken")
    public Object getOssToken() {
        AssumeRoleResponse response = stsService.assumeRole(
                ossProperties.getRegionId(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret(),
                ossProperties.getRoleArn(),
                "yinzhu-oss-session",
                null,
                3600L
        );
        if (response != null) {
            OssConfigResponse result = new OssConfigResponse();
            result.setCredentials(response.getCredentials());
            result.setDomain(ossProperties.getDomain());
            result.setRegionId(ossProperties.getRegionId());
            result.setBucket(ossProperties.getBucket());
            return result;
        }
        throw new SystemBusinessException("OssToken获取失败");
    }

}
