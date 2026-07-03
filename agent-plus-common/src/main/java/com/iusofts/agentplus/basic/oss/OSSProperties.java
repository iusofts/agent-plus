/*
 * Copyright (C) 2019 All rights reserved
 * Author: Ivan Shen
 * Date: 2019/9/5
 * Description:OSSProperties.java
 */
package com.iusofts.agentplus.basic.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Ivan Shen
 */
@Data
@Component
@ConfigurationProperties(prefix = "storage.oss")
public class OSSProperties {

    private String accessKeyId;

    private String accessKeySecret;

    private String endPoint;

    private String regionId;

    private String roleArn;
    
    private String domain;
    
    private String bucket;

}
