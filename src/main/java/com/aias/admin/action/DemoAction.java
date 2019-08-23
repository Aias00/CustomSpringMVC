package com.aias.admin.action;

import com.aias.admin.service.IDemoService;
import com.aias.admin.service.INamedService;
import com.aias.framework.annotation.MyAutowired;
import com.aias.framework.annotation.MyController;
import com.aias.framework.annotation.MyRequestMapping;
import com.aias.framework.annotation.MyRequestParam;
import com.aias.framework.servlet.MyModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@MyController
public class DemoAction {

    @MyAutowired
    private IDemoService demoService;

    @MyAutowired("myNameService")
    private INamedService namedService;


    @MyRequestMapping("/query")
    public MyModelAndView query(HttpServletRequest request, HttpServletResponse response, @MyRequestParam("name") String name) {
        out(response, "this is my name:" + name);
        return null;
    }
    @MyRequestMapping("/demo")
    public MyModelAndView demo(HttpServletRequest request, HttpServletResponse response, @MyRequestParam("name") String name,@MyRequestParam("addr") String addr) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", name);
        map.put("addr", addr);
        return new MyModelAndView("second.aias", map);
    }
    @MyRequestMapping("/get")
    public MyModelAndView get(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", "12312312 3123123123");
        return new MyModelAndView("first.aias", map);
    }

    @MyRequestMapping("/test")
    public MyModelAndView test(HttpServletRequest request, HttpServletResponse response) {
        out(response, "this is json string");
        return null;
    }


    private void out(HttpServletResponse response, String msg) {
        try {
            response.getWriter().write(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
