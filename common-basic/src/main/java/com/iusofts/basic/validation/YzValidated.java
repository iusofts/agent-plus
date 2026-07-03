package com.iusofts.basic.validation;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YzValidated {
    Class<?>[] groups() default {};
}