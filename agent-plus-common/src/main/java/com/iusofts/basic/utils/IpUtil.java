/*
 * Copyright (C) 2019 All rights reserved
 * Author: Ivan Shen
 * Date: 2019/12/15
 * Description:IpUtil.java
 */
package com.iusofts.basic.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * IP工具类
 *
 * @author Ivan Shen
 */
public class IpUtil {

    private static final String LOCAL_IP = "127.0.0.1";

    private static final String DATATYPE = "text";

    private static final String QUERY_IP_URI = "http://api.ip138.com/query/?ip=";

    private static final String QUERY_IP_TOKEN = "98f6471acc71da31fda80352ee0c4a1b";

    private static final int SUCCEED = 200;


    public static boolean internalIp(String ip) {
        byte[] addr = textToNumericFormatV4(ip);
        return internalIp(addr);
    }


    public static boolean internalIp(byte[] addr) {
        final byte b0 = addr[0];
        final byte b1 = addr[1];
        //10.x.x.x/8
        final byte SECTION_1 = 0x0A;
        //172.16.x.x/12
        final byte SECTION_2 = (byte) 0xAC;
        final byte SECTION_3 = (byte) 0x10;
        final byte SECTION_4 = (byte) 0x1F;
        //192.168.x.x/16
        final byte SECTION_5 = (byte) 0xC0;
        final byte SECTION_6 = (byte) 0xA8;
        switch (b0) {
            case SECTION_1:
                return true;
            case SECTION_2:
                if (b1 >= SECTION_3 && b1 <= SECTION_4) {
                    return true;
                }
            case SECTION_5:
                switch (b1) {
                    case SECTION_6:
                        return true;
                }
            default:
                return false;

        }
    }

    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return "0:0:0:0:0:0:0:1".equals(ip) ? LOCAL_IP : ip;
    }

    /**
     * 查询ip地理位置
     *
     * @param ip 8.8.8.8
     * @return
     */
    public static String queryIPLocation(String ip) {
        if ("127.0.0.1".equals(ip)) {
            return "本地环境";
        }
        String url = QUERY_IP_URI + ip + "&datatype=" + DATATYPE;
        return get(url);
    }

    private static String get(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.setReadTimeout(5 * 1000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("token", QUERY_IP_TOKEN);
            int responseCode = conn.getResponseCode();
            if (responseCode == SUCCEED) {
                StringBuilder builder = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                for (String s = br.readLine(); s != null; s = br.readLine()) {
                    builder.append(s);
                }
                br.close();
                return builder.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 等效原IPAddressUtil.textToNumericFormatV4(ip)，返回4字节byte数组
     * @param ip IPv4字符串（如192.168.1.1）
     * @return 4字节byte数组（非法IP返回null）
     */
    public static byte[] textToNumericFormatV4(String ip) {
        // 空值校验（和原方法一致）
        if (ip == null || ip.trim().isEmpty()) {
            return null;
        }

        try {
            // 1. 解析IP地址
            InetAddress inetAddress = InetAddress.getByName(ip);
            // 2. 校验是否为IPv4（排除IPv6/非法格式）
            if (!(inetAddress instanceof Inet4Address)) {
                return null;
            }
            // 3. 获取4字节的byte数组（和原方法返回值完全一致）
            byte[] addr = inetAddress.getAddress();
            // 校验字节数组长度（防止异常情况，原方法也会做此校验）
            return addr.length == 4 ? addr : null;
        } catch (UnknownHostException e) {
            // 非法IP格式（如192.168.1.256、abc.1.2.3）→ 返回null（和原方法一致）
            return null;
        }
    }

}
