package com.volunteer.common.annotation;

import java.lang.annotation.*;

/**
 * 敏感词校验注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckSensitive {
    /**
     * 需要校验的字段名列表
     */
    String[] fields() default { "content" };
}
