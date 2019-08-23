package com.aias.framework.handler;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * <b>
 * HandlerMapping定义
 * <br>
 */
public class Handler {
    private Object controller;

    private Method method;

    private Pattern pattern;

    public Handler(Object controller, Method method, Pattern pattern) {
        this.controller = controller;
        this.method = method;
        this.pattern = pattern;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
