package com.aias.framework.annotation;

import java.lang.annotation.*;

// 加到方法和类上
@Target({ElementType.METHOD,ElementType.TYPE})
// 运行时
@Retention(RetentionPolicy.RUNTIME)
// 只做说明使用
@Documented
public @interface MyRequestMapping {
    String value() default "";
}
