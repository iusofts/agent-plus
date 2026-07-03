package com.iusofts.agentplus.basic.validation;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApValidated {
    Class<?>[] groups() default {};
}