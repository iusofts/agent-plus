package com.iusofts.basic.oss;

import com.aliyun.oss.OSS;
import com.iusofts.basic.exception.SystemBusinessException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

/**
 * 封装的oss文件，图片处理的类 主要是对ossClient进行再封装
 *
 * @author Ivan
 */
@Slf4j
@Service
public class HandleService {

    @Resource
    private OSSProperties ossProperties;

    /**
     * 上传oss文件
     *
     * @param bucketName 目标bucket
     * @param objectKey  key路径
     * @return 返回上传地址
     */
    public String putOssObject(String bucketName, String objectKey, InputStream input) {
        log.info("HandleService putOssObject：[bucketName={},objectKey={},input={}]", bucketName, objectKey, "");
        OSS client = OSSClientSingleton.getOSSClient();
        client.putObject(bucketName, objectKey, input);
        return ossProperties.getDomain() + "/" + objectKey;
    }
    
    /**
     * 上传oss文件
     *
     * @param objectKey  key路径
     * @return 返回上传地址
     */
    public String putOssObject(String objectKey, InputStream input) {
        return putOssObject(ossProperties.getBucket(), objectKey, input);
    }

    /**
     * 上传oss文件
     *
     * @param objectKey  key路径
     * @return 返回上传地址
     */
    public String putOssObject(String objectKey, ByteArrayOutputStream out) {
        try {
            log.info("开始上传文件至OSS：{}", objectKey);

            try (ByteArrayInputStream bais = new ByteArrayInputStream(out.toByteArray())) {
                String excelUrl = putOssObject(objectKey, bais);
                log.info("OSS上传成功，文件地址：{}", excelUrl);
                return excelUrl;
            }
        } catch (Exception e) {
            log.error("上传阿里云OSS失败", e);
            throw new SystemBusinessException("上传文件服务器失败！");
        }
    }


    /**
     * 上传oss文件
     *
     * @param bucketName 目标bucket
     * @param objectKey  目标路径
     * @return 返回域名的url
     */
    public String putOssObject(String bucketName, String objectKey, File file) {
        OSS client = OSSClientSingleton.getOSSClient();
        client.putObject(bucketName, objectKey, file);
        return ossProperties.getDomain() + "/" + objectKey;
    }
}
