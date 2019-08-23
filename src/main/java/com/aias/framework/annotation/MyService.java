package com.aias.framework.annotation;

import java.lang.annotation.*;

// 加到class上
@Target(ElementType.TYPE)
// 运行时
@Retention(RetentionPolicy.RUNTIME)
// 只做说明使用
@Documented
public @interface MyService {

    String value() default "";
}
