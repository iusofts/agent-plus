package com.iusofts.basic.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;

/**
 * 属性文件获取工具类(仅json)
 */
public class JsonDataUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonDataUtils.class);

    /*
     * 根据json文件名称获取json配置文件数据
     *
     * @param fileName json文件名称前缀，如果在resource下直接写文件名，如果有路径，请在前面添加路径如："com/xxx/abc"
     */
    public static JSONObject getJsonObject(String fileName) {
        JSONObject jsonObject = new JSONObject();
        fileName += ".json";
        try {
            File jsonFile = ResourceUtils.getFile("classpath:" + fileName);
            String json = FileUtils.readFileToString(jsonFile, "utf-8");
            jsonObject = JSON.parseObject(json);
            LOGGER.debug(jsonObject.toJSONString());
        } catch (IOException e) {
            LOGGER.error("解析失败", e);
        }
        return jsonObject;
    }

    /*
     * 根据json文件名称获取json配置文件数据
     *
     * @param fileName json文件名称前缀，如果在resource下直接写文件名，如果有路径，请在前面添加路径如："com/xxx/abc"
     */
    public static JSONArray getJsonArray(String fileName) {
        JSONArray jsonArray = new JSONArray();
        fileName += ".json";
        try {
            File jsonFile = ResourceUtils.getFile("classpath:" + fileName);
            String json = FileUtils.readFileToString(jsonFile, "utf-8");
            jsonArray = JSON.parseArray(json);
            System.out.println();
            LOGGER.debug(jsonArray.toJSONString());
        } catch (IOException e) {
            LOGGER.error("解析失败", e);
        }
        return jsonArray;
    }

}