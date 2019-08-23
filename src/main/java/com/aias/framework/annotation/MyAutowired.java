package com.aias.framework.annotation;

import java.lang.annotation.*;

// 加到属性上
@Target(ElementType.FIELD)
// 运行时
@Retention(RetentionPolicy.RUNTIME)
// 只做说明使用
@Documented
public @interface MyAutowired {

    String value() default "";
}