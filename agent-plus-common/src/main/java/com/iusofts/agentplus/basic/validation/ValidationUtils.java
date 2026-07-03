package com.iusofts.agentplus.basic.validation;

import com.iusofts.agentplus.basic.exception.InvalidParamException;
import org.springframework.util.CollectionUtils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 校验工具类
 *
 * @author：Ivan @date： 2016年2月1日 下午7:31:54
 */
public class ValidationUtils {

    private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * 校验对象所有属性
     *
     * @param obj
     * @param groups 分组
     * @return
     */
    public static <T> ValidationResult validateEntity(T obj, Class<?>... groups) {
        ValidationResult result = new ValidationResult();
        Set<ConstraintViolation<T>> set = null;
        if (groups != null && groups.length > 0) {
            set = validator.validate(obj, groups);
        } else {
            set = validator.validate(obj, Default.class);
        }
        if (!CollectionUtils.isEmpty(set)) {
            result.setHasErrors(true);
            Map<String, String> errorMsg = new HashMap<String, String>();
            for (ConstraintViolation<T> cv : set) {
                errorMsg.put(cv.getPropertyPath().toString(), cv.getMessage());
            }
            result.setErrorMsg(errorMsg);
        }
        return result;
    }

    /**
     * 校验对象的单个属性
     *
     * @param obj
     * @param propertyName
     * @return
     */
    public static <T> ValidationResult validateProperty(T obj, String propertyName, Class<?>... groups) {
        ValidationResult result = new ValidationResult();
        Set<ConstraintViolation<T>> set = null;
        if (groups != null && groups.length > 0) {
            set = validator.validateProperty(obj, propertyName, groups);
        } else {
            set = validator.validateProperty(obj, propertyName, Default.class);
        }
        if (!CollectionUtils.isEmpty(set)) {
            result.setHasErrors(true);
            Map<String, String> errorMsg = new HashMap<String, String>();
            for (ConstraintViolation<T> cv : set) {
                errorMsg.put(propertyName, cv.getMessage());
            }
            result.setErrorMsg(errorMsg);
        }
        return result;
    }

    /**
     * 校验对象所有属性(校验不通过抛异常)
     *
     * @param obj
     * @param groups 分组
     * @return true:校验通过
     */
    public static <T> boolean validate(T obj, Class<?>... groups) {
        ValidationResult result = validateEntity(obj, groups);
        if (result.isHasErrors()) {
            throw new InvalidParamException("参数校验失败:" + result.getAllErrorMsg(), result.getErrorMsg());
        }
        return true;
    }

    /**
     * 校验对象所有属性
     *
     * @param obj
     * @param otherErrors 其它错误
     * @param groups      分组
     * @return true:校验通过
     */
    public static <T> boolean validate(T obj, Map<String, String> otherErrors, Class<?>... groups) {
        ValidationResult result = validateEntity(obj, groups);

        // 将其它错误信息汇总到校验结果中
        if (otherErrors != null && otherErrors.size() > 0) {
            result.setHasErrors(true);
            if (result.getErrorMsg() == null) {
                result.setErrorMsg(otherErrors);
            } else {
                result.getErrorMsg().putAll(otherErrors);
            }
        }

        if (result.isHasErrors()) {
            return false;
        }
        return true;
    }

}