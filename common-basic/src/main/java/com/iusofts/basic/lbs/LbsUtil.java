/*
 * Copyright (C) 2019 All rights reserved
 * Author: Ivan Shen
 * Date: 2021/8/19
 * Description:LbsUtil.java
 */
package com.iusofts.basic.lbs;

import com.alibaba.fastjson.JSONObject;
import com.iusofts.basic.exception.SystemBusinessException;
import com.iusofts.basic.http.HttpUtils;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

/**
 * 腾讯位置服务
 *
 * @author Ivan Shen
 */
public class LbsUtil {

    private static final String DOMAIN = "https://apis.map.qq.com";
    private static final String SECRET_KEY = "IKGES21f1VoLAq7AuxLoMO16qCKICvZ";
    private static final String KEY = "SYGBZ-B7QW3-QSH3J-YDE2L-Y2F5O-6FFAM";

    public static String getCityNameByLocation(String location) {
        String api = "/ws/geocoder/v1/?get_poi=1&key=" + KEY + "&location=" + location;
        String sig = String.valueOf(md5Hex(api + SECRET_KEY));
        api = DOMAIN + api + "&sig=" + sig;
        String res = HttpUtils.sendGet(api, null);
        JSONObject result = JSONObject.parseObject(res).getJSONObject("result");
        if (result == null) {
            throw new SystemBusinessException("位置获取失败");
        }
        String city = result.getJSONObject("address_component").getString("city");
        return city;
    }

    public static void main(String[] args) {
        String location = "39.984154,116.307490";
        String cityName = getCityNameByLocation(location);
        System.err.println(cityName);
    }
}
