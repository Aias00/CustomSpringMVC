package com.aias.framework.annotation;

import java.lang.annotation.*;

// 加到返回值上，没有return 就定义在方法上
@Target({ElementType.METHOD})
// 运行时
@Retention(RetentionPolicy.RUNTIME)
// 只做说明使用
@Documented
public @interface MyResponseBody {
}
