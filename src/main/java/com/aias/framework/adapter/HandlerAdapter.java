package com.aias.framework.adapter;

import com.aias.framework.handler.Handler;
import com.aias.framework.servlet.MyModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;

/**
 * <b>
 * 方法适配器
 * <br>
 */
public class HandlerAdapter {

    private Map<String, Integer> paramMapping;

    private Handler handler;

    public HandlerAdapter(Map<String, Integer> paramMapping, Handler handler) {
        this.paramMapping = paramMapping;
        this.handler = handler;
    }

    public Map<String, Integer> getParamMapping() {
        return paramMapping;
    }

    public void setParamMapping(Map<String, Integer> paramMapping) {
        this.paramMapping = paramMapping;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    /**
     * <pre>
     * 主要目的是用反射调用
     * </pre>
     *
     * @param request:
     * @param response:
     * @param handler:
     * @return void
     * @author hongyu.liu
     * @date 2019/8/22
     * @time 18:12
     */
    public MyModelAndView handle(HttpServletRequest request, HttpServletResponse response, Handler handler) throws Exception {
        // 为什么要传request,
        // 为什么要传response,
        // 为什么要传handler,
        Class[] paramTypes = handler.getMethod().getParameterTypes();
        // 要想给参数赋值，只能通过索引号来找到具体的某个参数

        // 用于存放所有的参数值，用于之后赋值
        Object[] paramValues = new Object[paramTypes.length];
        Map<String, String[]> paramMap = request.getParameterMap();
        for (Map.Entry<String, String[]> param : paramMap.entrySet()) {
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");
            if (!this.paramMapping.containsKey(param.getKey())) {
                continue;
            }
            int index = this.paramMapping.get(param.getKey());
            // 单个赋值是不行的
            paramValues[index] = castStringValue(value, paramTypes[index]);
        }
        // request和response要赋值
        // 判断参数列表是否包含request和response
        if (this.paramMapping.containsKey(HttpServletRequest.class.getName())) {
            int requestIndex = this.paramMapping.get(HttpServletRequest.class.getName());
            paramValues[requestIndex] = request;
        }
        if (this.paramMapping.containsKey(HttpServletResponse.class.getName())) {
            int responseIndex = this.paramMapping.get(HttpServletResponse.class.getName());
            paramValues[responseIndex] = response;
        }
        boolean isModelAndView = handler.getMethod().getReturnType() == MyModelAndView.class;
        Object result = handler.getMethod().invoke(handler.getController(), paramValues);
        if (isModelAndView) {
            return (MyModelAndView) result;
        }
        return null;
    }

    private Object castStringValue(String value, Class clazz) {
        if (clazz == String.class) {
            return value;
        } else if (clazz == Integer.class) {
            return Integer.valueOf(value);
        } else if (clazz == int.class) {
            return Integer.valueOf(value).intValue();
        } else {
            return null;
        }
    }

}
