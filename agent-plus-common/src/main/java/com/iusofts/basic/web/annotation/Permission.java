package com.iusofts.basic.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限
 *
 * @author Ivan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Permission {
    
    String value() default "";
    
    String[] values() default {};

    String role() default "";

    String[] roles() default {};
    
}
