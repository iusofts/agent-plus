package com.iusofts.agentplus.basic.web.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OriginResponseBody {
}