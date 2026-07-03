/*
 * Copyright (C) 2019 All rights reserved
 * Author: Ivan Shen
 * Date: 2020/9/17
 * Description:NumberUtil.java
 */
package com.iusofts.basic.utils;

/**
 * 数据工具类
 *
 * @author Ivan Shen
 */
public class NumberUtil {

    public static boolean equals(Integer source, Integer target) {
        if (source == null && target == null) {
            return true;
        } else if (source == null) {
            return false;
        } else {
            return source.equals(target);
        }
    }

    public static boolean notEquals(Integer source, Integer target) {
        return !equals(source, target);
    }

    // 安全转 int，失败返回默认值
    public static Integer toInt(String s, Integer defaultValue) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // 安全转 long
    public static Long toLong(String s, Long defaultValue) {
        try {
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // 安全转 double
    public static Double toDouble(String s, Double defaultValue) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
}
