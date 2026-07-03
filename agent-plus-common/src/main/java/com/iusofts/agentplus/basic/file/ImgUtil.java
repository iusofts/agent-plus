/*
 * Copyright (C) 2019 All rights reserved
 * Author: Ivan Shen
 * Date: 2021/4/12
 * Description:ImgUtil.java
 */
package com.iusofts.agentplus.basic.file;

import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 图片工具类
 *
 * @author Ivan Shen
 */
@Slf4j
public class ImgUtil {

    /**
     * 传入要下载的图片的url列表，将url所对应的图片下载到本地
     *
     * @param imgLink
     * @throws Exception
     */
    public static void downloadPicture(String imgLink, String path, String imgName) throws Exception {
        URL url;
        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;
        try {
            url = new URL(imgLink);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0");
            connection.setConnectTimeout(10 * 1000);
            connection.setReadTimeout(15 * 1000);
            inputStream = connection.getInputStream();
            byte[] buffer = new byte[1024];
            int length;
            fileOutputStream = new FileOutputStream(path + imgName);
            while ((length = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, length);
            }
        } catch (Exception e) {
            log.error("图片下载失败：", e);
            throw new SystemBusinessException("图片下载失败：" + imgLink);
        } finally {
            inputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
        }
    }


    /**
     * 传入要下载的图片的url，返回InputStream
     *
     * @param imgLink
     * @throws Exception
     */
    public static InputStream urlToInputStream(String imgLink) throws Exception {
        URL url = new URL(imgLink);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0");
        connection.setConnectTimeout(10 * 1000);
        connection.setReadTimeout(15 * 1000);
        return connection.getInputStream();
    }
}
