package com.iusofts.basic.utils;

import org.springframework.cglib.beans.BeanCopier;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 胡前培
 * @version 1.0.0
 * @ClassName BeanCopierUtils
 * @Description TODO
 * @createTime 2020/04/13 15:00
 */

public class BeanCopyUtils {

    static final Map<String, BeanCopier> BEAN_COPIERS = new HashMap<>();

    /**
     *
     * 复制
     * @param srcObj 源实体
     * @param destObj 目标实体
     */
    public static void copy(Object srcObj, Object destObj) {
        String key = genKey(srcObj.getClass(), destObj.getClass());
        BeanCopier copier = null;
        if (!BEAN_COPIERS.containsKey(key)) {
            copier = BeanCopier.create(srcObj.getClass(), destObj.getClass(), false);
            BEAN_COPIERS.put(key, copier);
        } else {
            copier = BEAN_COPIERS.get(key);
        }
        copier.copy(srcObj, destObj, null);
    }

    private static String genKey(Class<?> srcClazz, Class<?> destClazz) {
        return srcClazz.getName() + destClazz.getName();
    }

}
