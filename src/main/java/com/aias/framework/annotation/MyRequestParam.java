package com.aias.framework.annotation;

import java.lang.annotation.*;

// 加到方法参数上
@Target(ElementType.PARAMETER)
// 运行时
@Retention(RetentionPolicy.RUNTIME)
// 只做说明使用
@Documented
public @interface MyRequestParam {
    String value() default "";

    boolean requeired() default true;

}
