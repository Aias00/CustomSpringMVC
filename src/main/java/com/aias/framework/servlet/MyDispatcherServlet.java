package com.aias.framework.servlet;


import com.aias.framework.adapter.HandlerAdapter;
import com.aias.framework.annotation.MyController;
import com.aias.framework.annotation.MyRequestMapping;
import com.aias.framework.annotation.MyRequestParam;
import com.aias.framework.context.MyApplicationContext;
import com.aias.framework.handler.Handler;
import com.aias.framework.resolver.MyViewResolver;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyDispatcherServlet extends HttpServlet {


    private static final String LOCATION = "contextConfigLocation";
//    private Map<Pattern, Handler> handlerMapping = new HashMap<>();

    private List<Handler> handlerMappings = new ArrayList<>();

//    private Map<Handler, HandlerAdapter> handlerAdapterMapping = new HashMap<>();

    private List<HandlerAdapter> handlerAdapterMappings = new ArrayList<>();

    private List<MyViewResolver> viewResolvers = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }


    /**
     * <pre>
     * 在这里调用自己写得controller的方法
     * </pre>
     *
     * @param req:
     * @param resp:
     * @return void
     * @author hongyu.liu
     * @date 2019/8/22
     * @time 14:06
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("调用");
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception,Msg :" + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
            return;
        }
    }

    private Handler getHandler(HttpServletRequest request) {
        // 循环handlerMapping

        if (handlerMappings.isEmpty()) {
            return null;
        }

        String url = request.getRequestURI();

        String contextPath = request.getContextPath();

        url = url.replace(contextPath, "").replaceAll("/+", "/");

        for (Handler entry : handlerMappings) {
            Matcher matcher = entry.getPattern().matcher(url);
            if (!matcher.matches()) {
                continue;
            }
            return entry;
        }
        return null;
    }

    private HandlerAdapter getHandlerAdapter(Handler handler) {
        // 获取一个handlerAdapter
        for (HandlerAdapter handlerAdapter : handlerAdapterMappings) {
            if (handler.equals(handlerAdapter.getHandler())) {
                return handlerAdapter;
            }
        }
        return null;

    }


    private void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 先取出来一个Handler，从HandlerMapping中取
        Handler handler = getHandler(request);
        if (null == handler) {
            response.getWriter().write("404 Not Found");
            return;
        }
        // 再取出来一个适配器
        HandlerAdapter handlerAdapter = getHandlerAdapter(handler);
        // 再由适配器去调用我们具体的方法
        MyModelAndView mv = handlerAdapter.handle(request, response, handler);

        // 写一个模板框架
        // velocity # 取值
        // freemarker # 取值
        // jsp $ 取值

        // @{name} 取值

        applyDefaultViewName(response, mv);

    }

    private void applyDefaultViewName(HttpServletResponse response, MyModelAndView mv) throws Exception {
        if (null == mv) {
            return;
        }
        if (viewResolvers.isEmpty()) {
            return;
        }

        for (MyViewResolver viewResolver : viewResolvers) {
            if (!mv.getView().equals(viewResolver.getViewName())) {
                continue;
            }
            String result = viewResolver.parse(mv);

            if (null != result) {
                response.getWriter().write(result);
                break;
            }


        }

    }

    /**
     * <pre>
     * 初始化我们的IOC容器
     * </pre>
     *
     * @param config:
     * @return void
     * @author hongyu.liu
     * @date 2019/8/22
     * @time 14:07
     */
    @Override
    public void init(ServletConfig config) throws ServletException {

        // IOC容器必须要先初始化
        // 假装容器已经启动
        MyApplicationContext context = new MyApplicationContext(config.getInitParameter(LOCATION));

        Map<String, Object> ioc = context.getAll();
        System.out.println(ioc);
        System.out.println(ioc.get("demoAction"));

        //请求解析
        initMultipartResolver(context);
        //多语言、国际化
        initLocaleResolver(context);
        //主题View层的
        initThemeResolver(context);

        //============== 重要 ================
        //解析url和Method的关联关系(找到url对应的方法)
        initHandlerMappings(context);
        //适配器（匹配的过程）(通过方法找到方法所有的参数)
        initHandlerAdapters(context);
        //============== 重要 ================


        //异常解析
        initHandlerExceptionResolvers(context);
        //视图转发（根据视图名字匹配到一个具体模板）
        initRequestToViewNameTranslator(context);

        //============== 重要 ================
        //解析模板中的内容（拿到服务器传过来的数据，生成HTML代码）
        initViewResolvers(context);
        //============== 重要 ================

        initFlashMapManager(context);
        System.out.println("---------测试启动---------");

    }

    private void initFlashMapManager(MyApplicationContext context) {

    }

    private void initViewResolvers(MyApplicationContext context) {
        // 模板一般是不会放到webroot下
        // 而是放在WEB-INFO下或者classes下
        // 这样就避免了用户直接请求到模板框架
        // 加载模板的个数，存储到缓存中
        // 检查模板中的语法错误(暂时不做)

        String templateRoot = context.getConfig().getProperty("templateRoot");

        // 归根到底是一个普通的文件
        String rootPath = this.getClass().getClassLoader().getResource(templateRoot).getPath();
        File rootDir = new File(rootPath);
        for (File file : rootDir.listFiles()) {
            viewResolvers.add(new MyViewResolver(file.getName(), file));
        }
    }

    private void initRequestToViewNameTranslator(MyApplicationContext context) {

    }

    private void initHandlerExceptionResolvers(MyApplicationContext context) {

    }

    /**
     * <pre>
     * 初始化handlerAdapter
     * 主要是用来动态匹配参数，还要动态赋值
     * </pre>
     *
     * @param context:
     * @return void
     * @author hongyu.liu
     * @date 2019/8/22
     * @time 17:37
     */
    private void initHandlerAdapters(MyApplicationContext context) {
        if (handlerMappings.isEmpty()) {
            return;
        }
        // 参数类型作为key，参数的索引号作为value
        Map<String, Integer> paramMapping = new HashMap<>();
        // 只需要取出具体的某个方法
        for (Handler entry : handlerMappings) {
            // 把这个方法所有的参数全部获取到
            Class[] parameterTypes = entry.getMethod().getParameterTypes();
            // 参数有顺序，但是通过反射没法拿到参数的顺序
            // 匹配自定义参数列表
            for (int i = 0; i < parameterTypes.length; i++) {
                Class type = parameterTypes[i];
                if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                    paramMapping.put(type.getName(), i);
                }
            }

            Annotation[][] annotations = entry.getMethod().getParameterAnnotations();
            for (int i = 0; i < annotations.length; ++i) {
                for (Annotation annotation : annotations[i]) {
                    if (annotation instanceof MyRequestParam) {
                        String paramName = ((MyRequestParam) annotation).value();
                        if (!"".equals(paramName.trim())) {
                            paramMapping.put(paramName, i);
                        }
                    }
                }
            }
            handlerAdapterMappings.add(new HandlerAdapter(paramMapping, entry));
        }

    }

    private void initHandlerMappings(MyApplicationContext context) {
        // 只要是由controller修饰的类 里面的方法全部找出来，而且这个方法上应该要加上了RequestMapping注解
        // 如果没加注解，这个方法是不能被外界访问的
        // RqeustMapping上会配置一个url，一个url就对应一个方法，保存到map中

        Map<String, Object> ioc = context.getAll();

        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(MyController.class)) {
                continue;
            }

            String url = "";
            if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
                MyRequestMapping requestMapping = (MyRequestMapping) clazz.getAnnotation(MyRequestMapping.class);
                url = requestMapping.value();
            }

            // 扫描controller下面的所有的方法
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                    continue;
                }
                MyRequestMapping requestMapping = (MyRequestMapping) method.getAnnotation(MyRequestMapping.class);
//                String mappingUrl = url + requestMapping.value();
                // 把url解析成正则
                String regex = url + requestMapping.value().replaceAll("/+", "/");

                Pattern pattern = Pattern.compile(regex);

                handlerMappings.add(new Handler(entry.getValue(), method, pattern));
                System.out.println("Mapping:" + regex + " " + method.toString());
            }


        }


    }

    private void initThemeResolver(MyApplicationContext context) {

    }

    private void initLocaleResolver(MyApplicationContext context) {

    }

    private void initMultipartResolver(MyApplicationContext context) {

    }
}
