package com.iusofts.basic.validation;


import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 校验结果
 *
 * @author：Ivan
 * @date： 2016年2月1日 下午7:31:36
 */
public class ValidationResult {

    /**
     * 校验结果是否有错
     */
    private boolean hasErrors;

    /**
     * 校验错误信息(key:对象属性名,value:错误信息)
     */
    private Map<String, String> errorMsg;

    public boolean isHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    /**
     * 校验错误信息(key:对象属性名,value:错误信息)
     *
     * @return
     * @author：Ivan
     * @date：2016年2月1日 下午7:46:37
     */
    public Map<String, String> getErrorMsg() {
        return errorMsg;
    }

    /**
     * 获取全部错误信息
     * @return
     */
    public String getAllErrorMsg() {
        String msg = "";
        if (errorMsg != null) {
            for (Map.Entry entry : errorMsg.entrySet()) {
                msg += entry.getValue() + ",";
            }
        }
        if(StringUtils.isNotBlank(msg)) {
            msg = msg.substring(0, msg.length() - 1);
        }
        return msg;
    }

    public void setErrorMsg(Map<String, String> errorMsg) {
        this.errorMsg = errorMsg;
    }

    /**
     * 添加错误信息
     * (业务逻辑判断时使用)
     *
     * @param key   属性名
     * @param value 错误信息
     * @author Ivan
     * @date 2016年6月3日 上午10:49:19
     */
    public void addError(String key, String value) {
        hasErrors = true;
        if (errorMsg == null) {
            errorMsg = new HashMap<String, String>();
        }
        errorMsg.put(key, value);
    }

    @Override
    public String toString() {
        return "ValidationResult [hasErrors=" + hasErrors + ", errorMsg="
                + errorMsg + "]";
    }

}