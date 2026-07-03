package com.iusofts.basic.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OSSClientSingleton {

    @Autowired
    private OSSProperties ossProperties;

    private volatile static OSS singleton;

    private OSSClientSingleton() {
    }

    private static String accessKeyId;

    private static String accessKeySecret;

    private static String endPoint;

    @PostConstruct
    public void beforeInit() {
        accessKeyId = ossProperties.getAccessKeyId();
        accessKeySecret = ossProperties.getAccessKeySecret();
        endPoint = ossProperties.getEndPoint();
    }

    public static OSS getOSSClient() {
        if (singleton == null) {
            synchronized (OSSClient.class) {
                if (singleton == null) {
                    singleton = new OSSClientBuilder().build(endPoint, accessKeyId, accessKeySecret);
                }
            }
        }
        return singleton;
    }

}
