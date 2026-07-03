package com.iusofts.agentplus.basic.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;

public class JsonValidUtil {

    /**
     * 校验字符串必须是：合法JSON + 必须是对象 {} 或 数组 []
     * 拒绝：1、true、null、"字符串" 这种顶层简单值
     */
    public static boolean isValidJsonObjectOrArray(String content) {
        // 第一步：先判断是不是合法 JSON
        if (!JSON.isValid(content)) {
            return false;
        }

        // 第二步：解析后判断是不是 对象/数组
        Object obj = JSON.parse(content);
        return (obj instanceof JSONObject) || (obj instanceof JSONArray);
    }
}